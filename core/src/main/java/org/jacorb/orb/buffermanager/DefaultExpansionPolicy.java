package org.jacorb.orb.buffermanager;

import org.jacorb.config.Configurable;
import org.jacorb.config.Configuration;
import org.jacorb.config.ConfigurationException;

public class DefaultExpansionPolicy implements BufferManagerExpansionPolicy, Configurable
{
    private double scale;
    private double divider;
    
    public void configure (Configuration configuration) throws ConfigurationException
    {
        scale = configuration.getAttributeAsFloat ("jacorb.buffermanager.defaultexpansionpolicy.scale", 4);
        divider = configuration.getAttributeAsFloat ("jacorb.buffermanager.defaultexpansionpolicy.divider", 6);
    }
    
    public int getExpandedSize (int requestedSize)
    {
        double multiplier = scale - Math.log (requestedSize) / divider;
        multiplier = (multiplier < 1.0) ? 1.0 : multiplier;
        return (int) Math.floor ( multiplier * requestedSize );
    }
}
