#@IgnoreInspection BashAddShebang

function _ok_social_debug ()
{
  echo "$*" >> /tmp/oksocialcached.test
  return
}

function _oksocial_is_cache_valid ()
{
  local cache_file cur regex
  cache_file=$1
  cur=$2

  _ok_social_debug "checking $cache_file '$cur'"
  if [ -f "$cache_file" ]; then
    regex=$(head -n 1 $cache_file)
    _ok_social_debug "regex $regex"

    if [[ "$cur" =~ ^$regex$ ]]; then
      _ok_social_debug "match"
      return 1
    else
      _ok_social_debug "no match"
      return 0
    fi
  else
    _ok_social_debug "no regex"

    return 0
  fi
}

function _oksocial_complete ()
{
  local cur prev words cword cache_file paths
  COMPREPLY=()
	job="${COMP_WORDS[0]}"
	cur="${COMP_WORDS[COMP_CWORD]}"
	prev="${COMP_WORDS[COMP_CWORD-1]}"

  case $prev in
        -d | --data | -H | --header | -A | --user-agent | --connect-timeout | --read-timeout \
        | -e | --referer | --cache | --token | --resolve | --certificatePin | --keystore \
        | --socks | -s | --set | --cert | --clientauth)
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
            COMPREPLY=($(compgen -W 'http/1.1 h2' -- "$curlast"))
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

  cache_file=$TMPDIR$(basename $job)-complete.cache

  if _oksocial_is_cache_valid $cache_file $cur; then
    _ok_social_debug compute

    paths=$(COMPLETION_FILE=$cache_file $job --urlCompletion "$cur")

    _ok_social_debug result $(wc -l $cache_file)
  else
    _ok_social_debug cached

    paths=$(tail -n +2 $cache_file)
  fi

  COMPREPLY=( $( compgen -o nospace -W "$paths" -- "$cur" ) )

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
complete -F _oksocial_complete slackapi
complete -F _oksocial_complete squareapi
complete -F _oksocial_complete stackexchangeapi
complete -F _oksocial_complete surveymonkeyapi
complete -F _oksocial_complete twilioapi
complete -F _oksocial_complete twitterapi
complete -F _oksocial_complete uberapi
