/**
 * Copyright (C) 2006-2010 The Parancoe Team <info@parancoe.org>
 *
 * This file is part of Parancoe Core.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.parancoe.persistence.util;

import org.lambico.test.spring.hibernate.DBTest;
import org.parancoe.persistence.po.hibernate.AuthorTC;
import org.parancoe.persistence.po.hibernate.BookTC;
import org.parancoe.persistence.po.hibernate.EntityTC;

/**
 * A base class for Parancoe-core tests.
 *
 * @author lucio
 */
public abstract class BaseTest extends DBTest {

    @Override
    public Class[] getFixtureClasses() {
        return new Class[]{EntityTC.class, BookTC.class, AuthorTC.class};
    }
}
