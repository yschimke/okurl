rootProject.name = "okurl"

val isCirrusServer = System.getenv().containsKey("CIRRUS_CI")
val isMasterBranch = System.getenv()["CIRRUS_BRANCH"] == "master"

buildCache {
  local(DirectoryBuildCache::class) {
    isEnabled = !isCirrusServer
    isPush = true
    removeUnusedEntriesAfterDays = 2
  }
  remote(HttpBuildCache::class) {
    url = uri("http://" + System.getenv().getOrDefault("CIRRUS_HTTP_CACHE_HOST", "localhost:12321") + "/")
    isEnabled = isCirrusServer
    isPush = isMasterBranch
  }
}


sourceControl {
  gitRepository(uri("https://github.com/yschimke/oksocial-output.git")) {
    producesModule("com.baulsupp:oksocial-output")
  }
}
