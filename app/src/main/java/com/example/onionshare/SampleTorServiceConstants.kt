package com.example.onionshare

interface SampleTorServiceConstants {
    companion object {

        val TOR_APP_USERNAME = "org.torproject.android"

        val DIRECTORY_TOR_BINARY = "bin"
        val DIRECTORY_TOR_DATA = "data"

        //name of the tor C binary
        val TOR_ASSET_KEY = "tor"

        //torrc (tor config file)
        val TORRC_ASSET_KEY = "torrc"
        val TORRCDIAG_ASSET_KEY = "torrcdiag"
        val TORRC_TETHER_KEY = "torrctether"

        val TOR_CONTROL_COOKIE = "control_auth_cookie"

        //privoxy
        val POLIPO_ASSET_KEY = "polipo"

        //privoxy.config
        val POLIPOCONFIG_ASSET_KEY = "torpolipo.conf"

        //geoip data file asset key
        val GEOIP_ASSET_KEY = "geoip"
        val GEOIP6_ASSET_KEY = "geoip6"

        //various console cmds
        val SHELL_CMD_CHMOD = "chmod"
        val SHELL_CMD_KILL = "kill -9"
        val SHELL_CMD_RM = "rm"
        val SHELL_CMD_PS = "toolbox ps"
        val SHELL_CMD_PS_ALT = "ps"


        //String SHELL_CMD_PIDOF = "pidof";
        val SHELL_CMD_LINK = "ln -s"
        val SHELL_CMD_CP = "cp"


        val CHMOD_EXE_VALUE = "770"

        val FILE_WRITE_BUFFER_SIZE = 1024

        val IP_LOCALHOST = "127.0.0.1"
        val UPDATE_TIMEOUT = 1000
        val TOR_TRANSPROXY_PORT_DEFAULT = 9040

        val STANDARD_DNS_PORT = 53
        val TOR_DNS_PORT_DEFAULT = 5400
        val TOR_VPN_DNS_LISTEN_ADDRESS = "127.0.0.1"

        val CONTROL_PORT_DEFAULT = 9051
        val HTTP_PROXY_PORT_DEFAULT = 8118 // like Privoxy!
        val SOCKS_PROXY_PORT_DEFAULT = 9050


        //path to check Tor against
        val URL_TOR_CHECK = "https://check.torproject.org"

        //control port
        val TOR_CONTROL_PORT_MSG_BOOTSTRAP_DONE = "Bootstrapped 100%"
        val LOG_NOTICE_HEADER = "NOTICE"
        val LOG_NOTICE_BOOTSTRAPPED = "Bootstrapped"
    }
}
