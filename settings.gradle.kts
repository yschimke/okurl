rootProject.name = "okurl"

val isCiServer = System.getenv().containsKey("CI")
val isMasterBranch = System.getenv()["CIRRUS_BRANCH"] == "master"

buildCache {
  local(DirectoryBuildCache::class) {
    isEnabled = !isCiServer
    isPush = true
    removeUnusedEntriesAfterDays = 2
  }
  remote(HttpBuildCache::class) {
    url = uri("http://" + System.getenv().getOrDefault("CIRRUS_HTTP_CACHE_HOST", "localhost:12321") + "/")
    isEnabled = isCiServer
    isPush = isMasterBranch
  }
}
