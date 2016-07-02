/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.example;

import com.example.data.DataBinder;
import com.example.resource.TeamsResource;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

/**
 *
 * @author PaulSamsotha
 */
public class SimpleApplication extends Application<SimpleConfiguration> {

    public static void main(String[] args) throws Exception {
        new SimpleApplication().run(args);
    }

    @Override
    public void initialize(Bootstrap<SimpleConfiguration> bootstrap) {
        bootstrap.addBundle(new AssetsBundle("/docs", "/docs", "index.html"));
    }

    @Override
    public void run(SimpleConfiguration config, Environment env) throws Exception {
        env.jersey().register(TeamsResource.class);
        env.jersey().register(new DataBinder());
    }
}
