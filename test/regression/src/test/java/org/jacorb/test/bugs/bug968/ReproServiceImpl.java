package org.jacorb.test.bugs.bug968;

import cxf.repro.ReproData;
import cxf.repro.ReproDatas;
import cxf.repro.ReproService;

public class ReproServiceImpl implements ReproService
{
    public int works( ReproData data )
    {
        return data.getALong();
    }

    public ReproDatas failsEmpty( ReproData data )
    {
        ReproDatas out = new ReproDatas();
        out.getItem().add( data );
        return out;
    }

    public ReproData failsCrash( ReproData data )
    {
        return data;
    }
}
