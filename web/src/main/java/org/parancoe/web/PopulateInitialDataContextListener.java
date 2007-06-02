// Copyright 2006-2007 The Parancoe Team
// 
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// 
// http://www.apache.org/licenses/LICENSE-2.0
// 
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package org.parancoe.web;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContextEvent;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.parancoe.persistence.dao.generic.GenericDao;
import org.parancoe.util.FixtureHelper;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;

/**
 * @author Paolo Dona paolo.dona@seesaw.it
 * @author Michele Franzin michele.franzin@seesaw.it
 */
public class PopulateInitialDataContextListener extends ContextLoaderListener {

    private static final Logger log = Logger.getLogger(PopulateInitialDataContextListener.class);

    private ApplicationContext ctx;

    @Override
    public void contextInitialized(ServletContextEvent evt) {
        Set<Class> fixtureClasses = new LinkedHashSet<Class>(getFixtureClasses());
        if (CollectionUtils.isEmpty(fixtureClasses)) {
            log.info("Skipping initial data population (no models)");
            return;
        }
        try {
            ctx = (ApplicationContext) evt.getServletContext().getAttribute(
                    WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
            Map<Class, Object[]> fixtures = FixtureHelper.loadFixturesFromResource("initialData/",
                    fixtureClasses);
            log.info("Populating initial data for models...");
            for (Class clazz : fixtures.keySet()) {
                if (ArrayUtils.isEmpty(fixtures.get(clazz))) {
                    log.warn("Population of " + FixtureHelper.getModelName(clazz)
                            + " skipped (empty fixture file?)");
                    continue;
                }
                populateTableForModel(clazz, fixtures.get(clazz));
            }
            fixtures.clear();
            log.info("Populating initial data for models done!");
        } catch (Exception e) {
            log.error("Error while populating initial data for models " + e.getMessage(), e);
        }
    }

    private List<Class> getFixtureClasses() {
        return new ArrayList<Class>();
    }

    private void populateTableForModel(Class clazz, Object[] fixtures) {
        String fixtureName = FixtureHelper.getModelName(clazz);
        GenericDao dao = (GenericDao) ctx.getBean(FixtureHelper.getFixtureDaoId(clazz));
        int count = dao.findAll().size();
        if (count == 0) {
            log.info("Populating " + fixtureName + " with " + fixtures.length + " items...");
            FixtureHelper.populateDbForModel(clazz, fixtures, dao);
            log.info("Population of " + fixtureName + " done!");
        } else {
            log.info("Population of " + fixtureName + " skipped (already populated)");
        }
    }

}