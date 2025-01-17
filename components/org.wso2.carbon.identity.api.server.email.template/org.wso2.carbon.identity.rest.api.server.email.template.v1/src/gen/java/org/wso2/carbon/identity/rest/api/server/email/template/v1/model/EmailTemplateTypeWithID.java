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
import java.util.List;
import org.wso2.carbon.identity.rest.api.server.email.template.v1.model.EmailTemplateType;
import org.wso2.carbon.identity.rest.api.server.email.template.v1.model.EmailTemplateWithID;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;
import javax.validation.Valid;
import javax.xml.bind.annotation.*;

public class EmailTemplateTypeWithID extends EmailTemplateType {
  
    private String id;

    /**
    * Unique id of the email template type.
    **/
    public EmailTemplateTypeWithID id(String id) {

        this.id = id;
        return this;
    }
    
    @ApiModelProperty(example = "YWNjb3VudGNvbmZpcm1hdGlvbg", value = "Unique id of the email template type.")
    @JsonProperty("id")
    @Valid
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }



    @Override
    public boolean equals(java.lang.Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        
        EmailTemplateTypeWithID emailTemplateTypeWithID = (EmailTemplateTypeWithID) o;
        return Objects.equals(this.id, emailTemplateTypeWithID.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("class EmailTemplateTypeWithID {\n");
        sb.append("    ").append(toIndentedString(super.toString())).append("\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
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

