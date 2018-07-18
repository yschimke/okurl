#@IgnoreInspection BashAddShebang

function _okurl_debug ()
{
  echo "$*" >> /tmp/okurlcached.test
  return
}

function _okurl_is_cache_valid ()
{
  local cache_file cur regex
  cache_file=$1
  cur=$2

  _okurl_debug "checking $cache_file '$cur'"
  if [[ -f "$cache_file" ]]; then
    regex=$(head -n 1 ${cache_file})
    _okurl_debug "regex $regex"

    if [[ ! -z "$regex" && "$cur" =~ ^$regex$ ]]; then
      _okurl_debug "match"
      return 0
    else
      _okurl_debug "no match"
      return 1
    fi
  else
    _okurl_debug "no regex"

    return 1
  fi
}

function _okurl_complete ()
{
  local cur prev words cword cache_file paths
  COMPREPLY=()
	job="${COMP_WORDS[0]}"
	cur="${COMP_WORDS[COMP_CWORD]}"
	prev="${COMP_WORDS[COMP_CWORD-1]}"

  idx=0
  tokenset=
  for i in "${COMP_WORDS[@]}"; do
    #_okurl_debug i $i
    idx=$(expr ${idx} + 1)
    if [[ "$i" = "-s" ]]; then
      tokenset=${COMP_WORDS[$idx]}
    fi
  done

  case ${prev} in
        -d | --data | -H | --header | --user-agent | --connect-timeout | --read-timeout \
        | --referer | --cache | --token | --resolve | --certificatePin | --keystore \
        | --socks | --proxy | --cert | --clientauth | --dnsServers | --user \
        | --ping-interval)
            return
            ;;
        --authorize)
            _okurl_service=${_okurl_service=$(okurl --complete service)}
            COMPREPLY=( $( compgen -W "${_okurl_service}" -- "$cur" ) )
            return
            ;;
        --ip)
            _okurl_ipmode=${_okurl_ipmode=$(okurl --complete ipmode)}
            COMPREPLY=( $( compgen -W "${_okurl_ipmode}" -- "$cur" ) )
            return
            ;;
        --dns)
            _okurl_dnsmode=${_okurl_dnsmode=$(okurl --complete dnsmode)}
            COMPREPLY=( $( compgen -W "${_okurl_dnsmode}" -- "$cur" ) )
            return
            ;;
        --protocols)
            _okurl_protocol=${_okurl_protocol=$(okurl --complete protocol)}
            COMPREPLY=( $( compgen -W "${_okurl_protocol}" -- "$cur" ) )
            return
            ;;
        -X|--request)
            _okurl_method=${_okurl_method=$(okurl --complete method)}
            COMPREPLY=( $( compgen -W "${_okurl_method}" -- "$cur" ) )
            return
            ;;
        -s|--set)
            _okurl_tokenset=${_okurl_tokenset=$(okurl --complete tokenset)}
            COMPREPLY=( $( compgen -W "${_okurl_tokenset}" -- "$cur" ) )
            return
            ;;
        --connectionSpec)
            _okurl_spec=${_okurl_spec=$(okurl --complete connectionSpec)}
            COMPREPLY=( $( compgen -W "${_okurl_spec}" -- "$cur" ) )
            return
            ;;
        --cipherSuite)
            _okurl_cipher=${_okurl_cipher=$(okurl --complete cipherSuite)}
            COMPREPLY=( $( compgen -W "${_okurl_cipher}" -- "$cur" ) )
            return
            ;;
        --tlsVersions)
            _okurl_tlsversion=${_okurl_tlsversion=$(okurl --complete tlsVersions)}
            COMPREPLY=( $( compgen -W "${_okurl_tlsversion}" -- "$cur" ) )
            return
            ;;
        --complete)
            _okurl_complete=${_okurl_complete=$(okurl --complete complete)}
            COMPREPLY=( $( compgen -W "${_okurl_complete}" -- "$cur" ) )
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
      _okurl_options=${_okurl_options:=$(_parse_help okurl --help)}
      COMPREPLY=( $( compgen -W "$_okurl_options" -- "$cur" ) )
      return;
  fi

  _get_comp_words_by_ref -n : cur

  cache_file=$TMPDIR$(basename ${job})${tokenset:+-$tokenset}-complete.cache

  if _okurl_is_cache_valid ${cache_file} ${cur}; then
    _okurl_debug cached

    paths=$(tail -n +2 ${cache_file})
  else
    _okurl_debug compute ${job} ${tokenset:+-s $tokenset} --urlCompletion "$cur"

    paths=$(COMPLETION_FILE=${cache_file} ${job} ${tokenset:+-s $tokenset} --urlCompletion "$cur")

    if [[ -f "$cache_file" ]]; then
      _okurl_debug result $(wc -l ${cache_file})
    else
      _okurl_debug missing ${cache_file}
    fi
  fi

  COMPREPLY=( $( compgen -o nospace -W "$paths" -- "$cur" ) )

  # bash 4
  #compopt -o nospace

  __ltrim_colon_completions "$cur"
}

complete -F _okurl_complete okurl

complete -F _okurl_complete 4sqapi
complete -F _okurl_complete fbapi
complete -F _okurl_complete fitbitapi
complete -F _okurl_complete giphyapi
complete -F _okurl_complete githubapi
complete -F _okurl_complete imgurapi
complete -F _okurl_complete igapi
complete -F _okurl_complete linkedinapi
complete -F _okurl_complete lyftapi
complete -F _okurl_complete mapboxapi
complete -F _okurl_complete msftapi
complete -F _okurl_complete slackapi
complete -F _okurl_complete spotifyapi
complete -F _okurl_complete squareapi
complete -F _okurl_complete stackexchangeapi
complete -F _okurl_complete surveymonkeyapi
complete -F _okurl_complete transferwiseapi
complete -F _okurl_complete twilioapi
complete -F _okurl_complete twitterapi
complete -F _okurl_complete uberapi
