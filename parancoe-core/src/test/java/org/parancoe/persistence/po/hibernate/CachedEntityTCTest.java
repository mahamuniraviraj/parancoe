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
package org.parancoe.persistence.po.hibernate;

import org.parancoe.persistence.dao.generic.CachedEntityTCDao;
import org.parancoe.persistence.util.BaseTest;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Tests on caching DAO methods.
 *
 * @author <a href="mailto:lucio@benfante.com">Lucio Benfante</a>
 * @version $Revision$
 */
public class CachedEntityTCTest extends BaseTest {

    public static final int COUNT = 1000;
    public static final String FIELD = "%67%";
    private static final int INVOCATIONS = 100;
    @Autowired
    private CachedEntityTCDao dao;

    @Override
    public Class[] getFixtureClasses() {
        return new Class[]{};
    }

    @Override
    protected void prepareTestInstance() throws Exception {
        super.prepareTestInstance();
        for (long i = 0; i < COUNT; i++) {
            dao.create(new CachedEntityTC("" + i, "" + i, "" + i, i));
        }
    }

    @Override
    protected void endTransaction() {
        dao.deleteAll();
        super.endTransaction();
    }

    public void testAllSize() {
        assertSize(COUNT, dao.findAll());
    }

    public void test1() {
        long startTime;

        // Caches due to @CachteIt annotation
        dao.findByFieldOne(FIELD);
        startTime = System.currentTimeMillis();
        for (int i = 0; i < INVOCATIONS; i++) {
            dao.findByFieldOne(FIELD);
        }
        long timeCacheItAutoGenerated = System.currentTimeMillis() - startTime;

        // Caches due to NamedQuery hints
        dao.findCacheByFieldTwo(FIELD);
        startTime = System.currentTimeMillis();
        for (int i = 0; i < INVOCATIONS; i++) {
            dao.findCacheByFieldTwo(FIELD);
        }
        long timeCacheItNamedQuery = System.currentTimeMillis() - startTime;

        // Senza la cache. Non viene messa in cache.
        dao.findNoCacheByFieldThree(FIELD);
        startTime = System.currentTimeMillis();
        for (int i = 0; i < INVOCATIONS; i++) {
            dao.findNoCacheByFieldThree(FIELD);
        }
        long timeNoCache = System.currentTimeMillis() - startTime;

        System.out.println("NoCache: " + timeNoCache + ", Hint: "
                + timeCacheItNamedQuery + ", CacheIt: "
                + timeCacheItAutoGenerated);
        assertTrue(timeNoCache > timeCacheItNamedQuery * 2);
        assertTrue(timeNoCache > timeCacheItAutoGenerated * 2);
    }
}