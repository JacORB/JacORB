package org.jacorb.test.bugs.bug876;

import static org.junit.Assert.assertTrue;
import org.jacorb.test.harness.ORBTestCase;
import org.jacorb.test.harness.TestUtils;
import org.junit.Test;

public class Bug876Test extends ORBTestCase
{
    @Test
    public void testUnionToString()
    {
        RushIdent rid = new RushIdent () ;
        rid.first = 1 ;
        rid.second = 2 ;

        PositionData pd = new PositionData () ;
        pd.poolFrame = 2 ;
        pd.poolID = 3 ;
        pd.rushFrame = 4 ;
        pd.skew = 5 ;
        pd.rushID = rid ;

        ServerFragmentData sfd = new ServerFragmentData () ;
        sfd.videoFragmentData (pd) ;

        ServerFragment sf = new ServerFragment () ;
        sf.start = 10 ;
        sf.finish = 20 ;
        sf.trackNum = 1 ;
        sf.fragmentData = sfd ;

        assertTrue (sf.toString().contains("poolFrame"));
        assertTrue (sfd.toString().contains("rushFrame"));
        assertTrue (pd.toString().contains("PositionData"));
        assertTrue (rid.toString().contains("second"));

        TestUtils.getLogger().debug("---------") ;
        TestUtils.getLogger().debug("sf=" + sf) ;
        TestUtils.getLogger().debug("---------") ;
        TestUtils.getLogger().debug("sfd=" + sfd) ;
        TestUtils.getLogger().debug("---------") ;
        TestUtils.getLogger().debug("pd=" + pd) ;
        TestUtils.getLogger().debug("---------") ;
        TestUtils.getLogger().debug("rid=" + rid) ;
        TestUtils.getLogger().debug("---------") ;
    }

}
