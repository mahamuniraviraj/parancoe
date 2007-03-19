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
package org.parancoe.persistence.security;

import org.parancoe.persistence.po.hibernate.EntityBase;

/**
 * A PO for User table.
 *
 * @author <a href="mailto:enrico.giurin@gmail.com">Enrico Giurin</a>
 * @version $Revision$
 */
@javax.persistence.Entity()
public class User extends EntityBase {
	private String username = null;
	private String password = null;
	private boolean enabled = true;
	public boolean isEnabled() {
		return enabled;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	
    
}