package com.github.klboke.nexusplus.server.docker;

import com.github.klboke.nexusplus.core.RepositoryType;
import com.github.klboke.nexusplus.protocol.docker.DockerConstants;
import com.github.klboke.nexusplus.protocol.docker.DockerDigest;
import com.github.klboke.nexusplus.protocol.docker.DockerErrorCode;
import com.github.klboke.nexusplus.protocol.docker.DockerProtocolException;
import com.github.klboke.nexusplus.server.cache.GroupMemberAssetCache;
import com.github.klboke.nexusplus.server.cache.NexusCacheType;
import com.github.klboke.nexusplus.server.maven.RepositoryRuntime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DockerGroupService {
  private final DockerHostedService hosted;
  private final DockerProxyService proxy;
  private final DockerManifestStore manifestStore;
  private final GroupMemberAssetCache groupMemberAssetCache;

  public DockerGroupService(DockerHostedService hosted, DockerProxyService proxy) {
    this(hosted, proxy, null, null);
  }

  @Autowired
  public DockerGroupService(
      DockerHostedService hosted,
      DockerProxyService proxy,
      DockerManifestStore manifestStore,
      GroupMemberAssetCache groupMemberAssetCache) {
    this.hosted = hosted;
    this.proxy = proxy;
    this.manifestStore = manifestStore;
    this.groupMemberAssetCache = groupMemberAssetCache;
  }

  public DockerResponse getManifest(RepositoryRuntime group, String imageName, String reference, boolean headOnly) {
    return getManifest(group, imageName, reference, headOnly, List.of());
  }

  public DockerResponse getManifest(
      RepositoryRuntime group,
      String imageName,
      String reference,
      boolean headOnly,
      List<String> acceptHeaders) {
    ensureGroup(group);
    RepositoryRuntime cached = cachedManifestMember(group, imageName, reference).orElse(null);
    if (cached != null) {
      try {
        DockerResponse response = getManifestFromMember(cached, imageName, reference, headOnly, acceptHeaders);
        rememberManifestHit(group, cached, imageName, reference);
        return response;
      } catch (DockerProtocolException e) {
        if (e.code() != DockerErrorCode.MANIFEST_UNKNOWN && e.code() != DockerErrorCode.NAME_UNKNOWN) {
          throw e;
        }
      }
    }
    for (RepositoryRuntime member : group.members()) {
      if (cached != null && member.id() == cached.id()) {
        continue;
      }
      try {
        DockerResponse response = getManifestFromMember(member, imageName, reference, headOnly, acceptHeaders);
        rememberManifestHit(group, member, imageName, reference);
        return response;
      } catch (DockerProtocolException e) {
        if (e.code() != DockerErrorCode.MANIFEST_UNKNOWN && e.code() != DockerErrorCode.NAME_UNKNOWN) {
          throw e;
        }
      }
    }
    throw new DockerProtocolException(DockerErrorCode.MANIFEST_UNKNOWN, reference);
  }

  public DockerResponse getBlob(RepositoryRuntime group, String imageName, DockerDigest digest, boolean headOnly) {
    ensureGroup(group);
    RepositoryRuntime cached = cachedBlobMember(group, imageName, digest).orElse(null);
    if (cached != null) {
      try {
        DockerResponse response = getBlobFromMember(cached, imageName, digest, headOnly);
        rememberBlobHit(group, cached, imageName, digest);
        return response;
      } catch (DockerProtocolException e) {
        if (e.code() != DockerErrorCode.BLOB_UNKNOWN && e.code() != DockerErrorCode.NAME_UNKNOWN) {
          throw e;
        }
      }
    }
    for (RepositoryRuntime member : group.members()) {
      if (cached != null && member.id() == cached.id()) {
        continue;
      }
      try {
        DockerResponse response = getBlobFromMember(member, imageName, digest, headOnly);
        rememberBlobHit(group, member, imageName, digest);
        return response;
      } catch (DockerProtocolException e) {
        if (e.code() != DockerErrorCode.BLOB_UNKNOWN && e.code() != DockerErrorCode.NAME_UNKNOWN) {
          throw e;
        }
      }
    }
    throw new DockerProtocolException(DockerErrorCode.BLOB_UNKNOWN, digest.value());
  }

  public DockerTagList tags(RepositoryRuntime group, String imageName, String last, int limit) {
    ensureGroup(group);
    LinkedHashSet<String> tags = new LinkedHashSet<>();
    boolean memberHasNext = false;
    int pageSize = Math.max(1, Math.min(limit, 1000));
    int memberLimit = pageSize + 1;
    for (RepositoryRuntime member : group.members()) {
      try {
        DockerTagList memberTags = switch (member.type()) {
          case HOSTED -> hosted.tags(member, imageName, last, memberLimit);
          case PROXY -> proxy.tags(member, imageName, last, memberLimit);
          case GROUP -> tags(member, imageName, last, memberLimit);
        };
        memberHasNext |= memberTags.hasNext();
        Object raw = memberTags.tags();
        if (raw instanceof List<?> list) {
          list.forEach(item -> {
            if (item != null) tags.add(item.toString());
          });
        }
      } catch (DockerProtocolException ignored) {
      }
    }
    List<String> sorted = new ArrayList<>(tags);
    sorted.sort(String::compareTo);
    if (last != null && !last.isBlank()) {
      sorted = sorted.stream().filter(tag -> tag.compareTo(last) > 0).toList();
    }
    boolean hasNext = sorted.size() > pageSize || (memberHasNext && sorted.size() >= pageSize);
    if (hasNext) {
      sorted = sorted.subList(0, pageSize);
    }
    return new DockerTagList(imageName, sorted, hasNext);
  }

  public DockerCatalogList catalog(RepositoryRuntime group, String last, int limit) {
    ensureGroup(group);
    LinkedHashSet<String> repositories = new LinkedHashSet<>();
    boolean memberHasNext = false;
    int pageSize = Math.max(1, Math.min(limit, 1000));
    int memberLimit = pageSize + 1;
    for (RepositoryRuntime member : group.members()) {
      try {
        DockerCatalogList memberCatalog = switch (member.type()) {
          case HOSTED -> hosted.catalog(member, last, memberLimit);
          case PROXY -> proxy.catalog(member, last, memberLimit);
          case GROUP -> catalog(member, last, memberLimit);
        };
        memberHasNext |= memberCatalog.hasNext();
        repositories.addAll(memberCatalog.repositories());
      } catch (DockerProtocolException ignored) {
      }
    }
    List<String> sorted = new ArrayList<>(repositories);
    sorted.sort(String::compareTo);
    if (last != null && !last.isBlank()) {
      sorted = sorted.stream().filter(repository -> repository.compareTo(last) > 0).toList();
    }
    boolean hasNext = sorted.size() > pageSize || (memberHasNext && sorted.size() >= pageSize);
    if (hasNext) {
      sorted = sorted.subList(0, pageSize);
    }
    return new DockerCatalogList(sorted, hasNext);
  }

  public Map<String, Object> referrers(
      RepositoryRuntime group, String imageName, DockerDigest digest, String artifactType) {
    ensureGroup(group);
    LinkedHashMap<String, Map<String, Object>> descriptors = new LinkedHashMap<>();
    for (RepositoryRuntime member : group.members()) {
      try {
        Object raw = switch (member.type()) {
          case HOSTED -> hosted.referrers(member, digest, artifactType).get("manifests");
          case PROXY -> proxy.referrers(member, imageName, digest, artifactType).get("manifests");
          case GROUP -> referrers(member, imageName, digest, artifactType).get("manifests");
        };
        if (raw instanceof List<?> list) {
          for (Object item : list) {
            if (item instanceof Map<?, ?> map) {
              Object rawDigest = map.get("digest");
              if (rawDigest != null && !rawDigest.toString().isBlank()) {
                descriptors.putIfAbsent(rawDigest.toString(), toStringObjectMap(map));
              }
            }
          }
        }
      } catch (DockerProtocolException ignored) {
      }
    }
    return Map.of(
        "schemaVersion", 2,
        "mediaType", DockerConstants.MEDIA_TYPE_OCI_INDEX,
        "manifests", List.copyOf(descriptors.values()));
  }

  private DockerResponse getManifestFromMember(
      RepositoryRuntime member,
      String imageName,
      String reference,
      boolean headOnly,
      List<String> acceptHeaders) {
    if (acceptHeaders == null || acceptHeaders.isEmpty()) {
      return switch (member.type()) {
        case HOSTED -> hosted.getManifest(member, imageName, reference, headOnly);
        case PROXY -> proxy.getManifest(member, imageName, reference, headOnly);
        case GROUP -> getManifest(member, imageName, reference, headOnly);
      };
    }
    return switch (member.type()) {
      case HOSTED -> hosted.getManifest(member, imageName, reference, headOnly, acceptHeaders);
      case PROXY -> proxy.getManifest(member, imageName, reference, headOnly, acceptHeaders);
      case GROUP -> getManifest(member, imageName, reference, headOnly, acceptHeaders);
    };
  }

  private DockerResponse getBlobFromMember(
      RepositoryRuntime member, String imageName, DockerDigest digest, boolean headOnly) {
    return switch (member.type()) {
      case HOSTED -> hosted.getBlob(member, digest, headOnly);
      case PROXY -> proxy.getBlob(member, imageName, digest, headOnly);
      case GROUP -> getBlob(member, imageName, digest, headOnly);
    };
  }

  private void rememberManifestHit(
      RepositoryRuntime group, RepositoryRuntime member, String imageName, String reference) {
    if (groupMemberAssetCache == null) {
      return;
    }
    groupMemberAssetCache.put(group, manifestKey(imageName, reference), NexusCacheType.METADATA, member.id());
    if (manifestStore == null) {
      return;
    }
    try {
      DockerManifestStore.StoredManifest stored = manifestStore.getManifest(member, imageName, reference);
      groupMemberAssetCache.put(group, blobKey(imageName, DockerDigest.parse(stored.manifest().digest())),
          NexusCacheType.CONTENT, member.id());
      for (String digest : manifestStore.referencedDigests(stored)) {
        groupMemberAssetCache.put(group, blobKey(imageName, DockerDigest.parse(digest)),
            NexusCacheType.CONTENT, member.id());
      }
    } catch (DockerProtocolException ignored) {
      // The member hit is still useful; descriptor-to-blob hints are best-effort.
    }
  }

  private void rememberBlobHit(RepositoryRuntime group, RepositoryRuntime member, String imageName, DockerDigest digest) {
    if (groupMemberAssetCache != null) {
      groupMemberAssetCache.put(group, blobKey(imageName, digest), NexusCacheType.CONTENT, member.id());
    }
  }

  private java.util.Optional<RepositoryRuntime> cachedManifestMember(
      RepositoryRuntime group, String imageName, String reference) {
    if (groupMemberAssetCache == null) {
      return java.util.Optional.empty();
    }
    return groupMemberAssetCache.get(group, manifestKey(imageName, reference), NexusCacheType.METADATA)
        .flatMap(memberId -> findMember(group, memberId));
  }

  private java.util.Optional<RepositoryRuntime> cachedBlobMember(
      RepositoryRuntime group, String imageName, DockerDigest digest) {
    if (groupMemberAssetCache == null) {
      return java.util.Optional.empty();
    }
    return groupMemberAssetCache.get(group, blobKey(imageName, digest), NexusCacheType.CONTENT)
        .flatMap(memberId -> findMember(group, memberId));
  }

  private java.util.Optional<RepositoryRuntime> findMember(RepositoryRuntime group, long repositoryId) {
    for (RepositoryRuntime member : group.members()) {
      if (member.id() == repositoryId) {
        return java.util.Optional.of(member);
      }
      if (member.isGroup()) {
        java.util.Optional<RepositoryRuntime> nested = findMember(member, repositoryId);
        if (nested.isPresent()) {
          return nested;
        }
      }
    }
    return java.util.Optional.empty();
  }

  private static String manifestKey(String imageName, String reference) {
    return "docker:manifest:" + imageName + ":" + reference;
  }

  private static String blobKey(String imageName, DockerDigest digest) {
    return "docker:blob:" + imageName + ":" + digest.value();
  }

  private static Map<String, Object> toStringObjectMap(Map<?, ?> source) {
    LinkedHashMap<String, Object> target = new LinkedHashMap<>();
    source.forEach((key, value) -> {
      if (key != null) {
        target.put(key.toString(), value);
      }
    });
    return Map.copyOf(target);
  }

  private static void ensureGroup(RepositoryRuntime runtime) {
    if (runtime.type() != RepositoryType.GROUP) {
      throw new DockerProtocolException(DockerErrorCode.UNSUPPORTED, "not a Docker group repository", 405);
    }
  }
}
