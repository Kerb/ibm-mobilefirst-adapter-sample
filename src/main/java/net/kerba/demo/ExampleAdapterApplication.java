package net.kerba.demo;

import com.ibm.mfp.adapter.api.MFPJAXRSApplication;

import java.util.logging.Logger;

/**
 * @user: kerb
 * @created: 24/07/2017.
 */
public class ExampleAdapterApplication extends MFPJAXRSApplication {

    private final static Logger logger = Logger.getLogger(ExampleAdapterApplication.class.getName());


    @Override
    protected void init() throws Exception {
        logger.info("ExampleAdapterApplication init()");
    }

    @Override
    protected void destroy() throws Exception {
        logger.info("ExampleAdapterApplication destroy()");
    }

    @Override
    protected String getPackageToScan() {
        return getClass().getPackage().getName();
    }
}
