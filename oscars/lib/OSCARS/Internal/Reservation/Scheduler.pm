#==============================================================================
package OSCARS::Internal::Reservation::Scheduler;

=head1 NAME

OSCARS::Internal::Reservation::Scheduler - Common functionality for scheduling.

=head1 SYNOPSIS

  use OSCARS::Internal::Reservation::Scheduler;

=head1 DESCRIPTION

Functionality common to finding pending reservations and expired reservations.

=head1 AUTHORS

David Robertson (dwrobertson@lbl.gov)

=head1 LAST MODIFIED

June 19, 2006

=cut


use strict;

use Data::Dumper;
use Error qw(:try);
use Socket;

use OSCARS::PSS::JnxLSP;
use OSCARS::Library::Reservation::Common;

use OSCARS::Method;
our @ISA = qw{OSCARS::Method};

sub initialize {
    my( $self ) = @_;

    $self->SUPER::initialize();
    $self->{LSP_SETUP} = 1;
    $self->{LSP_TEARDOWN} = 0;
    $self->{resvLib} = OSCARS::Library::Reservation::Common->new(
                            'user' => $self->{user}, 'db' => $self->{db});
    # must be overriden
    $self->{opstring} = 'incorrect';
} #____________________________________________________________________________


###############################################################################
# soapMethod:  handles setting up and tearing down LSP's.
# In:  reference to hash containing request parameters, and OSCARS::Logger 
#      instance
# Out: reference to hash containing response
#
sub soapMethod {
    my( $self, $request, $logger ) = @_;

    my $updateStatus;

    if ( !$self->{user}->authorized('Domains', 'manage') ) {
        throw Error::Simple(
            "User $self->{user}->{login} not authorized to manage circuits");
    }
    $request->{timeInterval} = 20;  #TODO:  FIX explicit setting
    if ($self->{opstring} eq 'setup') { $updateStatus = 'active'; }
    else { $updateStatus = 'finished'; }
    # find reservations that need to be scheduled
    my $reservations =
        $self->getReservations($request->{timeInterval});
    my @formattedList = ();
    for my $resv (@$reservations) {
        $self->mapToIPs($resv);
        # call PSS to schedule LSP
        $resv->{lspStatus} = $self->configurePSS($resv, $logger);
        $self->{resvLib}->updateReservation( $resv, $updateStatus, $logger );
        push( @formattedList, $self->{resvLib}->formatResults($resv) );
    }
    my $response = \@formattedList;
    return $response;
} #____________________________________________________________________________


## Private methods

###############################################################################
# configurePSS:  format the args and call pss to do the configuration change
#
sub configurePSS {
    my( $self, $resv, $logger ) = @_;   

    my( $error );

    # Create an LSP object.
    my $lsp_info = $self->mapFields($resv);
    $lsp_info->{logger} = $logger;
    $lsp_info->{db} = $self->{db};
    my $jnxLsp = new OSCARS::PSS::JnxLSP($lsp_info);
    $logger->info('LSP.' . $self->{opstring}, { 'id' => $resv->{id}  });
    $jnxLsp->configure_lsp($self->{opcode}, $logger);

    if ($error = $jnxLsp->get_error())  { return $error; }
    $logger->info('LSP.' . $self->{opstring} . '.complete', { 'id' => $resv->{id} });
    return "";
} #____________________________________________________________________________



###############################################################################
#
sub mapToIPs {
    my( $self, $resv ) = @_;
 
    # TODO:  FIX handling empty results
    my $statement = 'SELECT IP FROM hosts WHERE name = ?';
    my $row = $self->{db}->getRow($statement, $resv->{srcHost});
    $resv->{srcIP} = $row->{IP};
    $row = $self->{db}->getRow($statement, $resv->{destHost});
    $resv->{destIP} = $row->{IP};

    my $statement = 'SELECT name FROM topology.routers r ' .
        'INNER JOIN topology.interfaces i ON r.id = i.routerId ' .
        'WHERE i.id = ?';
   my $ipStatement = 'SELECT IP FROM topology.ipaddrs ip ' .
        'INNER JOIN topology.interfaces i ON i.id = ip.interfaceId ' .
        'INNER JOIN topology.routers r ON r.id = i.routerId ' .
        "WHERE r.name = ? AND ip.description = 'loopback'";
    # first get router name
    my $row = $self->{db}->getRow($statement, $resv->{ingressInterfaceId});
    if ( !$row->{name} ) { $resv->{ingressLoopbackIP} = undef; }
    else {     # given router name, get address
        $row = $self->{db}->getRow($ipStatement, $row->{name});
        $resv->{ingressLoopbackIP} = $row->{IP}; 
    }
    my $row = $self->{db}->getRow($statement, $resv->{egressInterfaceId});
    if ( !$row->{name} ) { $resv->{egressLoopbackIP} = undef; }
    else {
        $row = $self->{db}->getRow($ipStatement, $row->{name});
        $resv->{egressLoopbackIP} = $row->{IP}; 
    }
} #____________________________________________________________________________


###############################################################################
#
sub mapFields {
    my ( $self, $resv ) = @_;

    my ( %lsp_info );

    %lsp_info = (
      'name' => "oscars_$resv->{id}",
      'lsp_from' => $resv->{ingressLoopbackIP},
      'lsp_to' => $resv->{egressLoopbackIP},
      'bandwidth' => $resv->{bandwidth},
      'lsp_class-of-service' => $resv->{class},
      'policer_burst-size-limit' =>  $resv->{burstLimit},
      'source-address' => $resv->{srcIP},
      'destination-address' => $resv->{destIP},
    );
    if ($resv->{srcPort} && ($resv->{srcPort} != 'NULL')) {
        $lsp_info{'source-port'} = $resv->{srcPort};
    }
    if ($resv->{destPort} && ($resv->{destPort} != 'NULL')) {
        $lsp_info{'destination-port'} = $resv->{destPort};
    }
    #if ($resv->{dscp} && ($resv->{dscp} != 'NULL')) {
    #$lsp_info{dscp} = $resv->{dscp};
    #}
    if ($resv->{protocol} && ($resv->{protocol} != 'NULL')) {
        $lsp_info{protocol} = $resv->{protocol};
    }
    return \%lsp_info;
} #____________________________________________________________________________


######
1;
# vim: et ts=4 sw=4
