package net.es.oscars.bss;

import org.testng.annotations.*;

import java.util.List;
import java.util.Properties;
import org.hibernate.*;

import net.es.oscars.oscars.TypeConverter;
import net.es.oscars.PropHandler;
import net.es.oscars.database.HibernateUtil;
import net.es.oscars.pathfinder.*;


/**
 * This class tests BSS reservation-related methods called by SOAP and by WBUI.
 *
 * @author David Robertson (dwrobertson@lbl.gov)
 */
@Test(groups={ "bss" })
public class ReservationManagerTest {
    private final Long BANDWIDTH = 25000000L;   // 25 Mbps
    private final Long BURST_LIMIT = 10000000L; // 10 Mbps
    private final int DURATION = 240000;       // 4 minutes 
    private final String PROTOCOL = "UDP";
    private final String LSP_CLASS = "4";
    private ReservationManager rm;
    private Properties props;
    private SessionFactory sf;
    private String dbname;
    private TypeConverter tc;

  @BeforeClass
    protected void setUpClass() {
        PropHandler propHandler = new PropHandler("test.properties");
        this.props = propHandler.getPropertyGroup("test.common", true);
        this.dbname = "testbss";
        this.sf = HibernateUtil.getSessionFactory(dbname);
        this.rm = new ReservationManager(this.dbname);
        this.tc = new TypeConverter();
    }

  @Test
    public void testCreate() throws BSSException {
        Reservation resv = new Reservation();
        Long millis = 0L;
        Long bandwidth = 0L;
        String url = null;
        int id = -1;

        this.sf.getCurrentSession().beginTransaction();
        ReservationDAO dao = new ReservationDAO(this.dbname);
        resv.setSrcHost(this.props.getProperty("sourceHostIP"));
        resv.setDestHost(this.props.getProperty("destHostIP"));

        millis = System.currentTimeMillis();
        resv.setStartTime(millis);
        resv.setCreatedTime(millis);
        millis += DURATION;
        resv.setEndTime(millis);

        resv.setBandwidth(BANDWIDTH);
        resv.setBurstLimit(BURST_LIMIT);
        // description is unique, so can use it to access reservation in
        // other tests
        resv.setDescription(this.props.getProperty("description"));
        resv.setProtocol(PROTOCOL);
        resv.setLspClass(LSP_CLASS);
        String account = this.props.getProperty("login");

        try {
            url = this.rm.create(resv, account, null, null);
        } catch (BSSException ex) {
            this.sf.getCurrentSession().getTransaction().rollback();
            throw ex;
        }
        id = resv.getId();
        this.sf.getCurrentSession().getTransaction().commit();
    }

  @Test(dependsOnMethods={ "testCreate" })
    public void testQuery() throws BSSException {
        Reservation reservation = null;

        this.sf.getCurrentSession().beginTransaction();
        ReservationDAO dao = new ReservationDAO(this.dbname);
        String description = this.props.getProperty("description");
        Reservation testResv = dao.queryByParam("description", description);
        try {
            reservation = this.rm.query(this.tc.getReservationTag(testResv),
                                        testResv.getLogin(), true);
        } catch (BSSException ex) {
            this.sf.getCurrentSession().getTransaction().rollback();
            throw ex;
        }
        String testTag = this.tc.getReservationTag(reservation);
        String newTag = this.tc.getReservationTag(testResv);
        this.sf.getCurrentSession().getTransaction().commit();
        assert testTag.equals(newTag);
    }

  @Test(dependsOnMethods={ "testCreate" })
    public void testAuthList() throws BSSException {
        List<Reservation> reservations = null;

        this.sf.getCurrentSession().beginTransaction();
        ReservationDAO dao = new ReservationDAO(this.dbname);
        try {
            reservations = this.rm.list(this.props.getProperty("login"), true);
        } catch (BSSException ex) {
            this.sf.getCurrentSession().getTransaction().rollback();
            throw ex;
        }
        this.sf.getCurrentSession().getTransaction().commit();
        assert !reservations.isEmpty();
    }

  @Test(dependsOnMethods={ "testCreate" })
    public void testUserList() throws BSSException {
        List<Reservation> reservations = null;

        this.sf.getCurrentSession().beginTransaction();
        ReservationDAO dao = new ReservationDAO(this.dbname);
        String login = this.props.getProperty("login");
        try {
            reservations = this.rm.list(login, false);
        } catch (BSSException ex) {
            this.sf.getCurrentSession().getTransaction().rollback();
            throw ex;
        }
        this.sf.getCurrentSession().getTransaction().commit();
        assert !reservations.isEmpty();
    }

  @Test(dependsOnMethods={ "testCreate" })
    public void testCancel() throws BSSException {
        List<Reservation> reservations = null;

        this.sf.getCurrentSession().beginTransaction();
        ReservationDAO dao = new ReservationDAO(this.dbname);
        String description = this.props.getProperty("description");
        Reservation resv = dao.queryByParam("description", description);
        try {
            this.rm.cancel(this.tc.getReservationTag(resv), resv.getLogin(),
                           true);
        } catch (BSSException ex) {
            this.sf.getCurrentSession().getTransaction().rollback();
            throw ex;
        }
        this.sf.getCurrentSession().getTransaction().commit();
    }
}
