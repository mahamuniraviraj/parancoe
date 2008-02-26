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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.parancoe.util.BaseConf;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

public abstract class BaseFormController extends SimpleFormController {
    
    protected void initBinder(HttpServletRequest req, ServletRequestDataBinder binder) throws Exception {
        // @TODO: sistemare il property editor di default per le date
        binder.registerCustomEditor(Date.class, new CustomDateEditor(new SimpleDateFormat("dd/MM/yyyy"), true));
    }

    public ModelAndView genericError(Exception e) {
        getLogger().error(e.getMessage(), e);
        Map<Object, Object> params = new HashMap<Object, Object>();
        params.put("messageCode", e.getMessage());
        params.put("exception", e);
        return new ModelAndView("genericError", params);
    }

    public ModelAndView genericError(String messageCode) {
        getLogger().error(messageCode);
        Map<Object, Object> params = new HashMap<Object, Object>();
        params.put("messageCode", messageCode);
        return new ModelAndView("genericError", params);
    }

    public abstract Logger getLogger();
}
