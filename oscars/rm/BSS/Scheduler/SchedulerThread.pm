######################################################################
# Scheduler thread that polls the db looking for 
# reservataions that need to be scheduled
#
# Poll time now comes from the config file.
#
# JRLee
######################################################################
package BSS::Scheduler::SchedulerThread;

use threads;
use threads::shared;

use Data::Dumper;

    # Chins PSS module to configure the routers
use PSS::LSPHandler::JnxLSP;

    # Front end to reservations database
use BSS::Frontend::Reservation;

# try to keep it tight
use strict;

require Exporter;

our @ISA = qw(Exporter);
our @EXPORT = qw(start_scheduler);


################## CONSTANTS ##############################

use constant _LSP_SETUP => 1;
use constant _LSP_TEARDOWN => 0;


################## GLOBALS ##############################
# settings from global configuration file 
my ($configs);


#fake pss calls for the momment
my ($fakeit) = 0;

my ($_error);

######################################################################
# This should probably be just new or 
# some such OO thing. Just startup and deatch
# a thread to do scheduling, return control
# to the main prog
######################################################################
sub start_scheduler {

    # get a copy of the config
    $configs = shift;

    print STDERR "Starting Scheduler\n";
    my $handler = threads->create("scheduler");
    $handler->detach();

    return 1;
}

######################################################################
# Main: Loop forever checking the DB every N
# minutes for reserversations
######################################################################
sub scheduler {

    print STDERR "Scheduler running\n";
    my ($front_end, $result, $error_msg);

    $front_end = BSS::Frontend::Reservation->new('configs' => $configs);

    while (1) {

        # find reservations that need to be actived
        #print STDERR "find new\n";
        $error_msg = find_new_reservations($front_end);
        if ($error_msg) {
            print STDERR '** ', $error_msg, "\n";
        }


        # find reservations that need to be deactivated 
        #print STDERR "find_exp\n";
        $error_msg = find_expired_reservations($front_end);
        if ($error_msg) {
            print STDERR '** ', $error_msg, "\n";
        }

        # check every do_poll_time seconds
        sleep($configs->{'db_poll_time'});
    }
}

######################################################################
# Find reservations to run
# need to find all the reservatations in db
# that need to be setup and run in the next N
# minutes
######################################################################
sub find_new_reservations {


    my ($front_end) = @_;

    #print STDERR "in find_new_res\n";
    my ($timeslot, $resv, $result);
    my ($error_msg);
    my $cur_time = localtime();

    #print STDERR "declared vars...\n";
    # configurable
    $timeslot = time() + $configs->{'reservation_time_interval'};
    print STDERR "pending: $cur_time \n";

    # find reservations that need to be scheduled
    ($error_msg, $resv) = $front_end->find_pending_reservations($timeslot, $configs->{'PENDING'});
    if ($error_msg) {
        return ($error_msg);
    }

    foreach my $r (@$resv) {
        ## calls to pss to setup reservations
        my %lsp_info = map_fields($front_end, $r);
        $result = setup_pss(%lsp_info);

        #print STDERR "update reservation to active\n";
        update_reservation( $r, $result, $configs->{'ACTIVE'}, $front_end);
    }
    return "";
}

######################################################################
#
# Find reservations that have expired, and tear them down
#
######################################################################
sub find_expired_reservations {

    my ($front_end) = @_;

    my $cur_time = localtime();
    my ($timeslot, $resv, $result, $error_msg);

    # configurable
    $timeslot = time() + $configs->{reservation_time_interval};
    #print STDERR "expired: $cur_time \n";

    # find active reservation past the timeslot
    ($error_msg, $resv) = $front_end->find_expired_reservations($timeslot, $configs->{'ACTIVE'});
    if ($error_msg) { return $error_msg; }
       
    foreach my $r (@$resv) {
        my %lsp_info = map_fields($front_end, $r);
        $result = teardown_pss(%lsp_info, $front_end);

        #print STDERR "update reservation to active\n";
        update_reservation( $r, $result, $configs->{'FINISHED'}, $front_end);
    }
    return "";
}
######################################################################
#
# Format the args and call pss to do the configuraion change
#
######################################################################
sub setup_pss {

    my (%lspInfo) = @_;   

        # fill in remaining fields
    $lspInfo{'protocol'} = 'udp';
    $lspInfo{'source-port'} = '5000';

    print STDERR "execing pss to schedule reservations\n";

    if ($fakeit == 0 ) {
        # Create an LSP object.
        my ($_jnxLsp) = new PSS::LSPHandler::JnxLSP(%lspInfo);

        print STDERR "Setting up LSP...\n";
        $_jnxLsp->configure_lsp(_LSP_SETUP);
        if ($_error = $_jnxLsp->get_error())  {
            print STDERR $_error;
            return 0;
            #die($_error);
        }
    }
    print STDERR "LSP setup complete\n" ;
    return 1;
}

######################################################################
#
# Format the args and call pss to teardown the configuraion 
#
######################################################################
sub teardown_pss {

    my (%lspInfo) = @_;

        # fill in remaining fields
    $lspInfo{'protocol'} = 'udp';
    $lspInfo{'source-port'} = '5000';

    if ($fakeit == 0 ) {
        # Create an LSP object.
        my ($_jnxLsp) = new PSS::LSPHandler::JnxLSP(%lspInfo);

        print STDERR "Tearing down LSP...\n" ;
        $_jnxLsp->configure_lsp(_LSP_TEARDOWN); 
        if ($_error = $_jnxLsp->get_error())  {
            print STDERR $_error;
            return 0;
        }
    }
    print STDERR "LSP teardown complete\n" ;
    return 1;
}


######################################################################
#
# Change the status of the reservervation from pending to active
#
######################################################################

sub update_reservation {

    my ($resv, $result, $status, $front_end) = @_;

    if ( $result == 1 ) {
        print STDERR "Changing status to $status\n";
        $front_end->update_reservation($resv, $status)
    } else {
        print STDERR "Changing status to failed\n";
        $front_end->update_reservation($resv, $configs->{'FAILED'})
    }
}

######################################################################

sub map_fields
{
    my ( $front_end, $data ) = @_;
    my ( %results, $error );
    my ( $ingress_loopback_name, $egress_loopback_name, $src_ip, $dst_ip );

     # get loopbacks for routers, given interface ids
    ($ingress_loopback_name, $error) = $front_end->{'dbconn'}->xface_id_to_loopback($data->{'ingress_interface_id'});
    ($egress_loopback_name, $error) = $front_end->{'dbconn'}->xface_id_to_loopback($data->{'egress_interface_id'});
     # get host IP addresses, given id 
    ($src_ip, $error) = $front_end->{'dbconn'}->hostaddrs_id_to_ip($data->{'src_hostaddrs_id'});
    ($dst_ip, $error) = $front_end->{'dbconn'}->hostaddrs_id_to_ip($data->{'dst_hostaddrs_id'});
    %results = (
      'name' => "oscars_$data->{'reservation_id'}",
      #'lsp_from' => $ingress_loopback_name,
      'lsp_from' => 'dev-rt20-e.es.net',
      #'lsp_to' => $egress_loopback_name,
          # hard wired for now
      'lsp_to' => "10.0.0.1",
      'bandwidth' => $data->{'reservation_bandwidth'},
      'lsp_class-of-service' => $data->{'reservation_class'},
      'policer_burst-size-limit' =>  $data->{'reservation_burst_limit'},
      'source-address' => $src_ip,
      'destination-address' => $dst_ip,
      'dscp' => $data->{'reservation_dscp'}
    );
    return ( %results );
}


#########################
##  End of package
#########################
1;

# vim: et ts=4 sw=4
