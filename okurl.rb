class Okurl < Formula
  desc "Okurl"
  homepage "https://github.com/yschimke/okurl"
  version "0.1"
  url "file://#{Dir.pwd}/build/okurl.tar"

  depends_on :java

  def install
    libexec.install Dir["*"]
    bin.install_symlink "#{libexec}/build/graal/okurl"
    zsh_completion.install "#{libexec}/zsh/_okurl"
  end
end

