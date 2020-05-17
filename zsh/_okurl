#compdef okurl

_okurl() {
  local context state state_descr line
  typeset -A opt_args

  _arguments \
    '--user-agent=-[User-Agent to send to server]:userAgent' \
    '--connect-timeout=-[Maximum time allowed for connection (seconds). (0 = disabled)]:connectTimeout' \
    '--read-timeout=-[Maximum time allowed for reading data (seconds). (0 = disabled)]:readTimeout' \
    '--ping-interval=-[Interval between pings. (0 = disabled)]:pingInterval' \
    '(-k --insecure)-k[Allow connections to SSL sites without certs]:allowInsecure' \
    '(-k --insecure)--insecure[Allow connections to SSL sites without certs]:allowInsecure' \
    '(-i --include)-i[Include protocol headers in the output]:showHeaders' \
    '(-i --include)--include[Include protocol headers in the output]:showHeaders' \
    '--frames[Log HTTP/2 frames to STDERR]:showHttp2Frames' \
    '--debug[Debug]:debug' \
    '--cache=-[Cache directory]:cacheDirectory' \
    '--protocols=-[Protocols]:protocols' \
    '--tracing=-[Activate Zipkin Tracing]:tracing' \
    '--ip=-[IP Preferences (system, ipv4, ipv6, ipv4only, ipv6only)]:ipMode' \
    '--dns=-[DNS (netty, java, dnsoverhttps)]:dnsMode' \
    '--dnsServers=-[Specific DNS Servers (csv, google)]:dnsServers' \
    '--resolve=-[DNS Overrides (HOST:TARGET)]:resolve' \
    '--certificatePin=-[Certificate Pin to define host:pinsha]:certificatePins' \
    '--networkInterface=-[Specific Local Network Interface]:networkInterface' \
    '--clientauth[Use Client Authentication (from keystore)]:clientAuth' \
    '--keystore=-[Keystore]:keystoreFile' \
    '--cert=-[Use given server cert (Root CA)]:serverCerts' \
    '--connectionSpec=-[Connection Spec (RESTRICTED_TLS, MODERN_TLS, COMPATIBLE_TLS)]:connectionSpec' \
    '--cipherSuite=-[Cipher Suites]:cipherSuites' \
    '--tlsVersions=-[TLS Versions]:tlsVersions' \
    '--opensc=-[Send OpenSC Client Certificate (slot)]:opensc' \
    '--socks=-[Use SOCKS proxy]:socksProxy' \
    '--proxy=-[Use HTTP proxy]:proxy' \
    '--os-proxy[Use OS defined proxy]:osProxy' \
    '(-s --set)-s=-[Token Set e.g. work]:tokenSet' \
    '(-s --set)--set=-[Token Set e.g. work]:tokenSet' \
    '--ssldebug[SSL Debug]:sslDebug' \
    '--user=-[user:password for basic auth]:user' \
    '--maxrequests=-[Concurrency Level]:maxRequests' \
    '--curl[Show curl commands]:curl' \
    '(-r --raw)-r[Raw Output]:rawOutput' \
    '(-r --raw)--raw[Raw Output]:rawOutput' \
    '--localCerts=-[Local Certificates]:localCerts' \
    '--ct=-[Certificate Transparency]:certificateTransparency' \
    '--ctHost=-[Certificate Transparency]:certificateTransparencyHosts' \
    '(-X --request)-X=-[Specify request command to use]:method' \
    '(-X --request)--request=-[Specify request command to use]:method' \
    '(-d --data)-d=-[HTTP POST data]:data' \
    '(-d --data)--data=-[HTTP POST data]:data' \
    '(-H --header)-H=-[Custom header to pass to server]:headers' \
    '(-H --header)--header=-[Custom header to pass to server]:headers' \
    '--noFollow[Follow redirects]:dontFollowRedirects' \
    '--referer=-[Referer URL]:referer' \
    '(-o --output)-o=-[Output file]:outputDirectory' \
    '(-o --output)--output=-[Output file]:outputDirectory' \
    '--authorize[Authorize API]:authorize' \
    '--renew[Renew API Authorization]:renew' \
    '--remove[Remove API Authorization]:remove' \
    '--token=-[Use existing Token for authorization]:token' \
    '--showCredentials[Show Credentials]:showCredentials' \
    '--complete=-[Complete options]:complete' \
    '--urlCompletion[URL Completion]:urlComplete' \
    '--apidoc[API Documentation]:apiDoc' \
    '(-h --help)-h[Show this help message and exit.]:helpRequested' \
    '(-h --help)--help[Show this help message and exit.]:helpRequested' \
    '(-V --version)-V[Print version information and exit.]:versionRequested' \
    '(-V --version)--version[Print version information and exit.]:versionRequested' \
    '1:arguments'

  _message -r " -- warning: $state $keystoreFile"

#   case $state in
#     (authorize) compadd "$@" prod ;;
#     (*) compadd "$@" prod staging dev
#   esac
}

_okurl "$@"
