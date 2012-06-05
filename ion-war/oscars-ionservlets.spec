%define package_name ionservlets
%define service_name IONUIService
%define mvn_project_list common-libs,common-logging,common-soap,utils,database,authN,authZ,resourceManager,coordinator,lookup,api,oscars-war,ion-war
%define install_base /opt/oscars/%{package_name}
%define oscars_home /etc/oscars
%define log_dir /var/log/oscars
%define run_dir /var/run/oscars
%define relnum 1

Name:           oscars-%{package_name}
Version:        0.6
Release:        %{relnum}
Summary:        ION Servlet Interface 
License:        distributable, see LICENSE
Group:          Development/Libraries
URL:            http://code.google.com/p/oscars-idc/
Source0:        oscars-%{version}-%{relnum}.tar.gz
BuildRoot:      %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)
BuildRequires:  java-1.6.0-openjdk
BuildRequires:  java-1.6.0-openjdk-devel
BuildRequires:  perl
BuildArch:      noarch
Requires:       oscars-env
Requires:       java-1.6.0-openjdk
Requires:       chkconfig

%description
Provides Java servlet interface for use by ION web interfaces

%pre
/usr/sbin/groupadd oscars 2> /dev/null || :
/usr/sbin/useradd -g oscars -r -s /sbin/nologin -c "OSCARS User" -d /tmp oscars 2> /dev/null || :

%prep
%setup -q -n oscars-%{version}-%{relnum}

%clean
rm -rf %{buildroot}

%build
mvn -DskipTests --projects %{mvn_project_list} clean package

%install
#Clean out previous build
rm -rf %{buildroot}

#Run install target
mvn -DskipTests --projects %{mvn_project_list} install 

#Create directory structure for build root
mkdir -p %{buildroot}/%{install_base}/target
mkdir -p %{buildroot}/%{install_base}/sql

#Copy jar files and scripts
cp ion-war/target/*.war %{buildroot}/%{install_base}/target/
cp ion-war/sql/*.sql %{buildroot}/%{install_base}/sql/
install -m 755 ion-war/scripts/configure_database %{buildroot}/%{install_base}/sql/

%post
#Setup database
%{install_base}/sql/configure_database %{install_base}/sql

#Create symbolic links to latest version of jar files
ln -s %{install_base}/target/ion-war-%{version}-%{relnum}.war %{install_base}/target/%{package_name}.war
chown oscars:oscars %{install_base}/target/%{package_name}.war

#enable mysql on boot
/sbin/chkconfig mysqld on

%files
%defattr(-,oscars,oscars,-)
%{install_base}/target/*
%{install_base}/sql/*

%preun
if [ $1 -eq 0 ]; then
    unlink %{install_base}/target/%{package_name}.war
fi
