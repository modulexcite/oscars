/*
Javascript functions for main page
Last modified: March 29, 2005
Soo-yeon Hwang (dapi@umich.edu)
David Robertson (dwrobertson@lbl.gov)
*/

/* List of functions:
print_navigation_bar(activePage)
print_current_date()
hasClass(obj)
stripe(id)
*/

// ** prints navigation bar, with active page highlighted
function print_navigation_bar(activePage)
{
    document.write("<div>");
    document.write('<ul id="tabnav">');
    if (activePage == 'reservation')
    {
        document.write('<li><a href="#" class="active" title="Create a new reservation">Make a Reservation</a></li>');
    }
    else
    {
        document.write('<li><a href="reservation.phtml" title="Create a new reservation">Make a Reservation</a></li>');
    }
    if (activePage == 'reservationList')
    {
        document.write('<li><a href="#" class="active" title="View list of active reservations">View Reservations</a></li>');
    }
    else
    {
    
        document.write('<li><a href="reservationlist.phtml" title="View list of active reservations">View Reservations</a></li>');
    }
    if (activePage == 'myprofile')
    {
        document.write('<li><a href="#" class="active" title="View and/or edit your personal information">My Profile</a></li>');
    }
    else
    {
        document.write('<li><a href="myprofile.phtml" title="View and/or edit your personal information">My Profile</a></li>');
    }
    if (activePage == 'logout')
    {
        document.write('<li><a href="#" class="active" title="Logout from the service">Logout</a></li>');
    }
    else
    {
        document.write('<li><a href="logout.phtml" title="Logout from the service">Logout</a></li>');
    }
    document.write("</ul>");
    document.write("</div>");
}


// ** print current date (format: July 7, 2004) **
function print_current_date()
{
	currentDate = new Date();
	currentMonth = currentDate.getMonth();

	if ( currentMonth == 0 ) { currentMonthName = 'January'; }
	if ( currentMonth == 1 ) { currentMonthName = 'February'; }
	if ( currentMonth == 2 ) { currentMonthName = 'March'; }
	if ( currentMonth == 3 ) { currentMonthName = 'April'; }
	if ( currentMonth == 4 ) { currentMonthName = 'May'; }
	if ( currentMonth == 5 ) { currentMonthName = 'June'; }
	if ( currentMonth == 6 ) { currentMonthName = 'July'; }
	if ( currentMonth == 7 ) { currentMonthName = 'August'; }
	if ( currentMonth == 8 ) { currentMonthName = 'September'; }
	if ( currentMonth == 9 ) { currentMonthName = 'October';}
	if ( currentMonth == 10 ) { currentMonthName = 'November'; }
	if ( currentMonth == 11 ) { currentMonthName = 'December'; }

	document.write( currentMonthName + " " + currentDate.getDate() + ", " + currentDate.getFullYear() );
}

// ** apply zebra stripe to a table **
// Reference: http://www.alistapart.com/articles/zebratables/

// this function is need to work around
// a bug in IE related to element attributes
function hasClass(obj)
{
	var result = false;
	if ( obj.getAttributeNode("class") != null )
	{
		result = obj.getAttributeNode("class").value;
	}
	return result;
}

function stripe(id)
{
	// the flag we'll use to keep track of 
	// whether the current row is odd or even
	var even = false;

	// if arguments are provided to specify the colours
	// of the even & odd rows, then use the them;
	// otherwise use the following defaults:
	var evenColor = arguments[1] ? arguments[1] : "#fff";
	var oddColor = arguments[2] ? arguments[2] : "#eee";

	// obtain a reference to the desired table
	// if no such table exists, abort
	var table = document.getElementById(id);
	if (! table) { return; }

	// by definition, tables can have more than one tbody
	// element, so we'll have to get the list of child
	// <tbody>s
	var tbodies = table.getElementsByTagName("tbody");

	// and iterate through them...
	for (var h = 0; h < tbodies.length; h++)
	{
		// find all the <tr> elements... 
		var trs = tbodies[h].getElementsByTagName("tr");
  
		// ... and iterate through them
		for (var i = 0; i < trs.length; i++)
		{
			// avoid rows that have a class attribute
			// or backgroundColor style
			if (!hasClass(trs[i]) && ! trs[i].style.backgroundColor)
			{
				// get all the cells in this row...
				var tds = trs[i].getElementsByTagName("td");

				// and iterate through them...
				for (var j = 0; j < tds.length; j++)
				{
					var mytd = tds[j];
					// avoid cells that have a class attribute
					// or backgroundColor style
					if (! hasClass(mytd) && ! mytd.style.backgroundColor)
					{
						mytd.style.backgroundColor = even ? evenColor : oddColor;
					}
				}
			}

			// flip from odd to even, or vice-versa
			even =  ! even;
		}
	}
}
