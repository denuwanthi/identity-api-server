/*
* Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.wso2.carbon.identity.rest.api.server.email.template.v1.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;
import javax.validation.Valid;
import javax.xml.bind.annotation.*;

public class SimpleEmailTemplate  {
  
    private String id;
    private String location;

    /**
    * Unique ID of the email template. This id represents the locale of the email template.
    **/
    public SimpleEmailTemplate id(String id) {

        this.id = id;
        return this;
    }
    
    @ApiModelProperty(example = "en_US", required = true, value = "Unique ID of the email template. This id represents the locale of the email template.")
    @JsonProperty("id")
    @Valid
    @NotNull(message = "Property id cannot be null.")

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    /**
    * Location of the created/updated resource.
    **/
    public SimpleEmailTemplate location(String location) {

        this.location = location;
        return this;
    }
    
    @ApiModelProperty(example = "/t/{tenant-domain}/api/server/v1/email/template-types/YWNjb3VudGNvbmZpcm1hdGlvbg/templates/en_US", required = true, value = "Location of the created/updated resource.")
    @JsonProperty("location")
    @Valid
    @NotNull(message = "Property location cannot be null.")

    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
    }



    @Override
    public boolean equals(java.lang.Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SimpleEmailTemplate simpleEmailTemplate = (SimpleEmailTemplate) o;
        return Objects.equals(this.id, simpleEmailTemplate.id) &&
            Objects.equals(this.location, simpleEmailTemplate.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, location);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class SimpleEmailTemplate {\n");
        
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    location: ").append(toIndentedString(location)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
    * Convert the given object to string with each line indented by 4 spaces
    * (except the first line).
    */
    private String toIndentedString(java.lang.Object o) {

        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n");
    }
}

