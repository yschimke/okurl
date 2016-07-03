function _oksocial_complete ()
{
  local cur prev words cword _cached
  COMPREPLY=()
	job="${COMP_WORDS[0]}"
	cur="${COMP_WORDS[COMP_CWORD]}"
	prev="${COMP_WORDS[COMP_CWORD-1]}"

  case $prev in
        -d | --data | -H | --header | -A | --user-agent | --connect-timeout | --read-timeout \
        | -e | --referer | --cache | --token | --resolve | --certificatePin | --clientcert \
        | --socks | -s | --set )
            return
            ;;
        --authorize)
            _oksocial_services=${_oksocial_services:=$(oksocial --serviceNames)}
            COMPREPLY=( $( compgen -W "$_oksocial_services" -- "$cur" ) )
            return
            ;;
        --dns)
            COMPREPLY=( $( compgen -W "system ipv4 ipv6 ipv4only ipv6only" -- "$cur" ) )
            return
            ;;
        --networkInterface)
            _available_interfaces -a
            return
            ;;
        --output|-o)
            _filedir
            return
            ;;
        --protocols)
            COMPREPLY=($(compgen -W 'http/1.1 spdy/3.1 h2' -- "$curlast"))
            return
            ;;
        -X|--request)
            COMPREPLY=( $( compgen -W "GET HEAD POST DELETE PUT PATCH" -- "$cur" ) )
            return
            ;;
  esac

  if [[ $cur == -* ]]; then
      _oksocial_options=${_oksocial_options:=$(_parse_help oksocial --help)}
      COMPREPLY=( $( compgen -W "$_oksocial_options" -- "$cur" ) )
      return;
  fi

  _get_comp_words_by_ref -n : cur

  _cached=0

  # something cached even null
  if [ -n "${_oksocial_paths+xx}" -a "$job" == "$_oksocial_last_job" ]; then
    # exact match
    if [ "$cur" == "$_oksocial_last_cur" ]; then
      _cached=1
    elif [ "${cur#$_oksocial_last_cur}" != "${cur}" ]; then
      _cached=2
    fi
  fi

  #echo "$_cached '$cur' '$_oksocial_last_cur'" >> /tmp/cached.test

  if [ "$_cached" == "0" ]; then
    _oksocial_paths=$($job --urlCompletion "$cur")
    _oksocial_last_job="$job"
    _oksocial_last_cur="$cur"
  fi

  COMPREPLY=( $( compgen -o nospace -W "$_oksocial_paths" -- "$cur" ) )

  # bash 4
  #compopt -o nospace

  __ltrim_colon_completions "$cur"
}

complete -F _oksocial_complete oksocial

complete -F _oksocial_complete 4sqapi
complete -F _oksocial_complete fbapi
complete -F _oksocial_complete githubapi
complete -F _oksocial_complete imgurapi
complete -F _oksocial_complete lyftapi
complete -F _oksocial_complete sheetsuapi
complete -F _oksocial_complete squareapi
complete -F _oksocial_complete stackexchangeapi
complete -F _oksocial_complete surveymonkeyapi
complete -F _oksocial_complete twilioapi
complete -F _oksocial_complete twitterapi
complete -F _oksocial_complete uberapi
