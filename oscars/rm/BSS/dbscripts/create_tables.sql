CREATE DATABASE IF NOT EXISTS BSS;
USE BSS;

-- table for AS, e.g. ESnet
CREATE TABLE IF NOT EXISTS networks (
    network_id              INT NOT NULL AUTO_INCREMENT,
    network_name            VARCHAR(40) NOT NULL,
    PRIMARY KEY (network_id)
) type=MyISAM;

-- table for router description
CREATE TABLE IF NOT EXISTS routers (
    router_id               INT NOT NULL AUTO_INCREMENT,
    router_valid            BOOLEAN NOT NULL,
    router_name             VARCHAR(40) NOT NULL,
        -- loopback interface IP
    router_loopback         VARCHAR(40) NOT NULL,
        -- key of corresponding AS in networks table
    network_id              INT NOT NULL,
    PRIMARY KEY (router_id)
) type=MyISAM;

-- table for interface description
-- would need trigger updating paths if changed
CREATE TABLE IF NOT EXISTS interfaces (
    interface_id            INT NOT NULL AUTO_INCREMENT,
    interface_valid         BOOLEAN NOT NULL,
        -- bandwidth in bps
    interface_speed         BIGINT UNSIGNED NOT NULL,
        -- description
    interface_descr         VARCHAR(80),
    interface_alias         VARCHAR(80),
        -- key of corresponding router in routers table
    router_id               INT NOT NULL,
    PRIMARY KEY (interface_id)
) type=MyISAM;

-- table for router interface ip addresses
CREATE TABLE IF NOT EXISTS ipaddrs (
    ipaddr_id               INT NOT NULL AUTO_INCREMENT,
        -- IP address
    ipaddr_ip               VARCHAR(40) NOT NULL,
        -- key of corresponding interface in interfaces table
    interface_id            INT NOT NULL,
    PRIMARY KEY (ipaddr_id)
) type=MyISAM;

-- table for source and destination IP addresses
CREATE TABLE IF NOT EXISTS hostaddrs (
    hostaddr_id             INT NOT NULL AUTO_INCREMENT,
    hostaddr_ip             VARCHAR(40) NOT NULL,
    PRIMARY KEY (hostaddr_id)
) type=MyISAM;

-- table holding reservation information
-- this information is passed to the PSS
CREATE TABLE IF NOT EXISTS reservations (
    reservation_id          INT NOT NULL AUTO_INCREMENT,
      -- times are in seconds since epoch (UTC)
    reservation_start_time  DATETIME NOT NULL,
    reservation_end_time    DATETIME NOT NULL,
        -- time this entry was created
    reservation_created_time DATETIME NOT NULL,
        -- bandwidth requested (Mbps)
    reservation_bandwidth   BIGINT UNSIGNED NOT NULL,
        -- in Mbps
    reservation_burst_limit BIGINT UNSIGNED NOT NULL,
        -- user making the reservation
    user_dn                 VARCHAR(80) NOT NULL,
        -- pending, active, failed, precancel, or cancelled
    reservation_status      VARCHAR(16) NOT NULL,
    reservation_class       VARCHAR(24) NOT NULL,
      -- the following are optional fields
        -- source and destination ports
    reservation_src_port    SMALLINT UNSIGNED,
    reservation_dst_port    SMALLINT UNSIGNED,
        -- differentiated services code point
    reservation_dscp        CHAR(1),
        -- protocol used (0-255, or a protocol string, such as udp)
    reservation_protocol    VARCHAR(16),
        -- human readable identifier
    reservation_tag         VARCHAR(80),
        -- space separated list of addresses of routers in path
    reservation_path        VARCHAR(120),
    reservation_description VARCHAR(120),
      -- foreign keys (not optional)
        -- keys of ingress and egress interfaces in interfaces table
    ingress_interface_id    INT NOT NULL,
    egress_interface_id     INT NOT NULL,
        -- keys of source and destination addresses in hostaddrs table
    src_hostaddr_id         INT NOT NULL, 
    dst_hostaddr_id         INT NOT NULL, 
    PRIMARY KEY (reservation_id)
) type = MyISAM;
