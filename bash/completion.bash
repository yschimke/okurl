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
    regex=$(head -n 1 ${cache_file})
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

  idx=0
  tokenset=
  for i in "${COMP_WORDS[@]}"; do
    #_ok_social_debug i $i
    idx=$(expr ${idx} + 1)
    if [ "$i" = "-s" ]; then
      tokenset=${COMP_WORDS[$idx]}
    fi
  done

  _ok_social_debug tokenset ${tokenset}

  case ${prev} in
        -d | --data | -H | --header | --user-agent | --connect-timeout | --read-timeout \
        | --referer | --cache | --token | --resolve | --certificatePin | --keystore \
        | --socks | --proxy | --cert | --clientauth | --dnsServers | --user \
        | --ping-interval)
            return
            ;;
        --authorize)
            _oksocial_service=${_oksocial_service=$(oksocial --complete service)}
            COMPREPLY=( $( compgen -W "${_oksocial_service}" -- "$cur" ) )
            return
            ;;
        --ip)
            _oksocial_ipmode=${_oksocial_ipmode=$(oksocial --complete ipmode)}
            COMPREPLY=( $( compgen -W "${_oksocial_ipmode}" -- "$cur" ) )
            return
            ;;
        --dns)
            _oksocial_dnsmode=${_oksocial_dnsmode=$(oksocial --complete dnsmode)}
            COMPREPLY=( $( compgen -W "${_oksocial_dnsmode}" -- "$cur" ) )
            return
            ;;
        --protocols)
            _oksocial_protocol=${_oksocial_protocol=$(oksocial --complete protocol)}
            COMPREPLY=( $( compgen -W "${_oksocial_protocol}" -- "$cur" ) )
            return
            ;;
        -X|--request)
            _oksocial_method=${_oksocial_method=$(oksocial --complete method)}
            COMPREPLY=( $( compgen -W "${_oksocial_method}" -- "$cur" ) )
            return
            ;;
        -s|--set)
            _oksocial_tokenset=${_oksocial_tokenset=$(oksocial --complete tokenset)}
            COMPREPLY=( $( compgen -W "${_oksocial_tokenset}" -- "$cur" ) )
            return
            ;;
        --connectionSpec)
            _oksocial_spec=${_oksocial_spec=$(oksocial --complete connectionSpec)}
            COMPREPLY=( $( compgen -W "${_oksocial_spec}" -- "$cur" ) )
            return
            ;;
        --cipherSuite)
            _oksocial_cipher=${_oksocial_cipher=$(oksocial --complete cipherSuite)}
            COMPREPLY=( $( compgen -W "${_oksocial_cipher}" -- "$cur" ) )
            return
            ;;
        --tlsVersions)
            _oksocial_tlsversion=${_oksocial_tlsversion=$(oksocial --complete tlsVersions)}
            COMPREPLY=( $( compgen -W "${_oksocial_tlsversion}" -- "$cur" ) )
            return
            ;;
        --complete)
            _oksocial_complete=${_oksocial_complete=$(oksocial --complete complete)}
            COMPREPLY=( $( compgen -W "${_oksocial_complete}" -- "$cur" ) )
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
        --cache)
            _filedir
            return
            ;;
  esac

  if [[ ${cur} == -* ]]; then
      _oksocial_options=${_oksocial_options:=$(_parse_help oksocial --help)}
      COMPREPLY=( $( compgen -W "$_oksocial_options" -- "$cur" ) )
      return;
  fi

  _get_comp_words_by_ref -n : cur

  cache_file=$TMPDIR$(basename ${job})${tokenset:+-$tokenset}-complete.cache

  if _oksocial_is_cache_valid ${cache_file} ${cur}; then
    _ok_social_debug compute ${job} ${tokenset:+-s $tokenset} --urlCompletion "$cur"

    paths=$(COMPLETION_FILE=${cache_file} ${job} ${tokenset:+-s $tokenset} --urlCompletion "$cur")

    _ok_social_debug result $(wc -l ${cache_file})
  else
    _ok_social_debug cached

    if [ -f "$cache_file" ]; then
      paths=$(tail -n +2 ${cache_file})
    else
      _ok_social_debug missing cache file ${cache_file}
      paths=
    fi
  fi

  COMPREPLY=( $( compgen -o nospace -W "$paths" -- "$cur" ) )

  # bash 4
  #compopt -o nospace

  __ltrim_colon_completions "$cur"
}

complete -F _oksocial_complete oksocial

complete -F _oksocial_complete 4sqapi
complete -F _oksocial_complete fbapi
complete -F _oksocial_complete fitbitapi
complete -F _oksocial_complete giphyapi
complete -F _oksocial_complete githubapi
complete -F _oksocial_complete imgurapi
complete -F _oksocial_complete igapi
complete -F _oksocial_complete linkedinapi
complete -F _oksocial_complete lyftapi
complete -F _oksocial_complete mapboxapi
complete -F _oksocial_complete msftapi
complete -F _oksocial_complete slackapi
complete -F _oksocial_complete spotifyapi
complete -F _oksocial_complete squareapi
complete -F _oksocial_complete stackexchangeapi
complete -F _oksocial_complete surveymonkeyapi
complete -F _oksocial_complete transferwiseapi
complete -F _oksocial_complete twilioapi
complete -F _oksocial_complete twitterapi
complete -F _oksocial_complete uberapi
