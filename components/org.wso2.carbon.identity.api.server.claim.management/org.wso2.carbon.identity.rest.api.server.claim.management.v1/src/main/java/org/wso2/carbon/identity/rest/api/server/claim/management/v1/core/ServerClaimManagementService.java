/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.identity.rest.api.server.claim.management.v1.core;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.identity.api.server.claim.management.common.Constant;
import org.wso2.carbon.identity.api.server.common.ContextLoader;
import org.wso2.carbon.identity.api.server.common.error.APIError;
import org.wso2.carbon.identity.api.server.common.error.ErrorResponse;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataClientException;
import org.wso2.carbon.identity.claim.metadata.mgt.exception.ClaimMetadataException;
import org.wso2.carbon.identity.claim.metadata.mgt.model.AttributeMapping;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ClaimDialect;
import org.wso2.carbon.identity.claim.metadata.mgt.model.ExternalClaim;
import org.wso2.carbon.identity.claim.metadata.mgt.model.LocalClaim;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.rest.api.server.claim.management.v1.dto.AttributeMappingDTO;
import org.wso2.carbon.identity.rest.api.server.claim.management.v1.dto.ClaimDialectReqDTO;
import org.wso2.carbon.identity.rest.api.server.claim.management.v1.dto.ClaimDialectResDTO;
import org.wso2.carbon.identity.rest.api.server.claim.management.v1.dto.ExternalClaimReqDTO;
import org.wso2.carbon.identity.rest.api.server.claim.management.v1.dto.ExternalClaimResDTO;
import org.wso2.carbon.identity.rest.api.server.claim.management.v1.dto.LinkDTO;
import org.wso2.carbon.identity.rest.api.server.claim.management.v1.dto.LocalClaimReqDTO;
import org.wso2.carbon.identity.rest.api.server.claim.management.v1.dto.LocalClaimResDTO;
import org.wso2.carbon.identity.rest.api.server.claim.management.v1.dto.PropertyDTO;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response;

import static org.wso2.carbon.identity.api.server.claim.management.common.ClaimManagementDataHolder.getClaimMetadataManagementService;
import static org.wso2.carbon.identity.api.server.claim.management.common.Constant.CMT_PATH_COMPONENT;
import static org.wso2.carbon.identity.api.server.claim.management.common.Constant.ErrorMessage.ERROR_CODE_ATTRIBUTE_FILTERING_NOT_IMPLEMENTED;
import static org.wso2.carbon.identity.api.server.claim.management.common.Constant.ErrorMessage.ERROR_CODE_CLAIMS_NOT_FOUND_FOR_DIALECT;
import static org.wso2.carbon.identity.api.server.claim.management.common.Constant.ErrorMessage.ERROR_CODE_DIALECT_NOT_FOUND;
import static org.wso2.carbon.identity.api.server.claim.management.common.Constant.ErrorMessage.ERROR_CODE_ERROR_ADDING_DIALECT;
import static org.wso2.carbon.identity.api.server.claim.management.common.Constant.ErrorMessage.ERROR_CODE_ERROR_ADDING_EXTERNAL_CLAIM;
import static org.wso2.carbon.identity.api.server.claim.management.common.Constant.ErrorMessage.ERROR_CODE_ERROR_ADDING_LOCAL_CLAIM;
import static org.wso2.carbon.identity.api.server.claim.management.common.Constant.ErrorMessage.ERROR_CODE_ERROR_DELETING_DIALECT;
import static org.wso2.carbon.identity.api.server.claim.management.common.Constant.ErrorMessage.ERROR_CODE_ERROR_DELETING_EXTERNAL_CLAIM;
import static org.wso2.carbon.identity.api.server.claim.management.common.Constant.ErrorMessage.ERROR_CODE_ERROR_DELETING_LOCAL_CLAIM;
import static org.wso2.carbon.identity.api.server.claim.management.common.Constant.ErrorMessage.ERROR_CODE_ERROR_RETRIEVING_DIALECT;
import static org.wso2.carbon.identity.api.server.claim.management.common.Constant.ErrorMessage.ERROR_CODE_ERROR_RETRIEVING_DIALECTS;
import static org.wso2.carbon.identity.api.server.claim.management.common.Constant.ErrorMessage.ERROR_CODE_ERROR_RETRIEVING_EXTERNAL_CLAIM;
import static org.wso2.carbon.identity.api.server.claim.management.common.Constant.ErrorMessage.ERROR_CODE_ERROR_RETRIEVING_EXTERNAL_CLAIMS;
import static org.wso2.carbon.identity.api.server.claim.management.common.Constant.ErrorMessage.ERROR_CODE_ERROR_RETRIEVING_LOCAL_CLAIM;
import static org.wso2.carbon.identity.api.server.claim.management.common.Constant.ErrorMessage.ERROR_CODE_ERROR_RETRIEVING_LOCAL_CLAIMS;
import static org.wso2.carbon.identity.api.server.claim.management.common.Constant.ErrorMessage.ERROR_CODE_ERROR_UPDATING_DIALECT;
import static org.wso2.carbon.identity.api.server.claim.management.common.Constant.ErrorMessage.ERROR_CODE_ERROR_UPDATING_EXTERNAL_CLAIM;
import static org.wso2.carbon.identity.api.server.claim.management.common.Constant.ErrorMessage.ERROR_CODE_ERROR_UPDATING_LOCAL_CLAIM;
import static org.wso2.carbon.identity.api.server.claim.management.common.Constant.ErrorMessage.ERROR_CODE_EXTERNAL_CLAIM_CONFLICT;
import static org.wso2.carbon.identity.api.server.claim.management.common.Constant.ErrorMessage.ERROR_CODE_EXTERNAL_CLAIM_NOT_FOUND;
import static org.wso2.carbon.identity.api.server.claim.management.common.Constant.ErrorMessage.ERROR_CODE_FILTERING_NOT_IMPLEMENTED;
import static org.wso2.carbon.identity.api.server.claim.management.common.Constant.ErrorMessage.ERROR_CODE_INVALID_DIALECT_ID;
import static org.wso2.carbon.identity.api.server.claim.management.common.Constant.ErrorMessage.ERROR_CODE_INVALID_USERSTORE;
import static org.wso2.carbon.identity.api.server.claim.management.common.Constant.ErrorMessage.ERROR_CODE_LOCAL_CLAIM_CONFLICT;
import static org.wso2.carbon.identity.api.server.claim.management.common.Constant.ErrorMessage.ERROR_CODE_LOCAL_CLAIM_NOT_FOUND;
import static org.wso2.carbon.identity.api.server.claim.management.common.Constant.ErrorMessage.ERROR_CODE_PAGINATION_NOT_IMPLEMENTED;
import static org.wso2.carbon.identity.api.server.claim.management.common.Constant.ErrorMessage.ERROR_CODE_SORTING_NOT_IMPLEMENTED;
import static org.wso2.carbon.identity.api.server.claim.management.common.Constant.LOCAL_DIALECT;
import static org.wso2.carbon.identity.api.server.claim.management.common.Constant.LOCAL_DIALECT_PATH;
import static org.wso2.carbon.identity.api.server.claim.management.common.Constant.PROP_DESCRIPTION;
import static org.wso2.carbon.identity.api.server.claim.management.common.Constant.PROP_DISPLAY_NAME;
import static org.wso2.carbon.identity.api.server.claim.management.common.Constant.PROP_DISPLAY_ORDER;
import static org.wso2.carbon.identity.api.server.claim.management.common.Constant.PROP_READ_ONLY;
import static org.wso2.carbon.identity.api.server.claim.management.common.Constant.PROP_REG_EX;
import static org.wso2.carbon.identity.api.server.claim.management.common.Constant.PROP_REQUIRED;
import static org.wso2.carbon.identity.api.server.claim.management.common.Constant.PROP_SUPPORTED_BY_DEFAULT;
import static org.wso2.carbon.identity.api.server.common.Constants.V1_API_PATH_COMPONENT;
import static org.wso2.carbon.identity.api.server.common.ContextLoader.buildURI;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.CONFLICT;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

/**
 * Call internal osgi services to perform server claim management related operations.
 */
public class ServerClaimManagementService {

    private static final Log LOG = LogFactory.getLog(ServerClaimManagementService.class);
    private static final String REL_CLAIMS = "claims";

    /**
     * Add a claim dialect.
     *
     * @param claimDialectReqDTO claimDialectReqDTO.
     * @return Resource identifier.
     */
    public String addClaimDialect(ClaimDialectReqDTO claimDialectReqDTO) {

        try {
            getClaimMetadataManagementService().addClaimDialect(
                    createClaimDialect(claimDialectReqDTO),
                    ContextLoader.getTenantDomainFromContext());
        } catch (ClaimMetadataException e) {
            throw handleClaimManagementException(e, ERROR_CODE_ERROR_ADDING_DIALECT,
                    claimDialectReqDTO.getDialectURI());
        }

        return getResourceId(claimDialectReqDTO.getDialectURI());
    }

    /**
     * Delete a claim dialect.
     *
     * @param dialectId dialectId.
     */
    public void deleteClaimDialect(String dialectId) {

        try {
            getClaimMetadataManagementService().removeClaimDialect(
                    new ClaimDialect(base64DecodeId(dialectId)),
                    ContextLoader.getTenantDomainFromContext());
        } catch (ClaimMetadataException e) {
            throw handleClaimManagementException(e, ERROR_CODE_ERROR_DELETING_DIALECT, dialectId);
        }

    }

    /**
     * Retrieve a claim dialect.
     *
     * @param dialectId dialectId.
     * @return Claim dialect.
     */
    public ClaimDialectResDTO getClaimDialect(String dialectId) {

        try {
            List<ClaimDialect> claimDialectList = getClaimMetadataManagementService().getClaimDialects(
                    ContextLoader.getTenantDomainFromContext());
            String decodedDialectId;
            if (StringUtils.equals(dialectId, LOCAL_DIALECT_PATH)) {
                decodedDialectId = LOCAL_DIALECT;
            } else {
                decodedDialectId = base64DecodeId(dialectId);
            }
            ClaimDialect claimDialect = extractDialectFromDialectList(decodedDialectId, claimDialectList);

            if (claimDialect == null) {
                throw handleClaimManagementClientError(ERROR_CODE_DIALECT_NOT_FOUND, NOT_FOUND, dialectId);
            }

            return getClaimDialectResDTO(claimDialect);

        } catch (ClaimMetadataException e) {
            throw handleClaimManagementException(e, ERROR_CODE_ERROR_RETRIEVING_DIALECT, dialectId);
        }
    }

    /**
     * Retrieve all claim dialects.
     *
     * @param limit  limit (optional).
     * @param offset offset (optional).
     * @param filter filter (optional).
     * @param sort   sort (optional).
     * @return List of claim dialects.
     */
    public List<ClaimDialectResDTO> getClaimDialects(Integer limit, Integer offset, String filter, String sort) {

        handleNotImplementedCapabilities(limit, offset, filter, sort);
        try {
            List<ClaimDialect> claimDialectList = getClaimMetadataManagementService().getClaimDialects(
                    ContextLoader.getTenantDomainFromContext());

            return getClaimDialectResDTOs(claimDialectList);

        } catch (ClaimMetadataException e) {
            throw handleClaimManagementException(e, ERROR_CODE_ERROR_RETRIEVING_DIALECTS);
        }
    }

    /**
     * Update claim dialect.
     * Due to the current implementation of the resource identifier of a new resource,
     * the resource id of the claim dialect changes when an update occurs.
     *
     * @param dialectId          dialectId.
     * @param claimDialectReqDTO claimDialectReqDTO.
     * @return Resource identifier.
     */
    public String updateClaimDialect(String dialectId, ClaimDialectReqDTO claimDialectReqDTO) {

        try {
            // If the old and new dialect uri is the same we don't need to do a db update.
            if (!StringUtils.equals(base64DecodeId(dialectId), claimDialectReqDTO.getDialectURI())) {
                getClaimMetadataManagementService().renameClaimDialect(
                        createClaimDialect(base64DecodeId(dialectId)),
                        createClaimDialect(claimDialectReqDTO),
                        ContextLoader.getTenantDomainFromContext());
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Skipping db update as the old dialectURI and the new dialectURI is the same. " +
                            "DialectURI: " + claimDialectReqDTO.getDialectURI());
                }
            }
        } catch (ClaimMetadataException e) {
            throw handleClaimManagementException(e, ERROR_CODE_ERROR_UPDATING_DIALECT, dialectId);
        }

        // Since the dialects identifier has changed we have to send the new identifier in the location header.
        return getResourceId(claimDialectReqDTO.getDialectURI());
    }

    /**
     * Add a local claim.
     *
     * @param localClaimReqDTO localClaimReqDTO.
     * @return Resource identifier.
     */
    public String addLocalClaim(LocalClaimReqDTO localClaimReqDTO) {

        try {
            for (AttributeMappingDTO attributeMappingDTO :
                    localClaimReqDTO.getAttributeMapping()) {
                if (!isUserStoreExists(attributeMappingDTO.getUserstore())) {
                    throw handleClaimManagementClientError(ERROR_CODE_INVALID_USERSTORE, BAD_REQUEST,
                            attributeMappingDTO.getUserstore());
                }
            }

            getClaimMetadataManagementService().addLocalClaim(
                    createLocalClaim(localClaimReqDTO),
                    ContextLoader.getTenantDomainFromContext());
        } catch (ClaimMetadataException e) {
            throw handleClaimManagementException(e, ERROR_CODE_ERROR_ADDING_LOCAL_CLAIM,
                    localClaimReqDTO.getClaimURI());
        } catch (UserStoreException e) {
            throw handleException(e, ERROR_CODE_ERROR_ADDING_LOCAL_CLAIM, localClaimReqDTO.getClaimURI());
        }

        return getResourceId(localClaimReqDTO.getClaimURI());
    }

    /**
     * Delete a local claim.
     *
     * @param claimId claimId.
     */
    public void deleteLocalClaim(String claimId) {

        try {
            getClaimMetadataManagementService().removeLocalClaim(
                    base64DecodeId(claimId),
                    ContextLoader.getTenantDomainFromContext());
        } catch (ClaimMetadataException e) {
            throw handleClaimManagementException(e, ERROR_CODE_ERROR_DELETING_LOCAL_CLAIM, claimId);
        }

    }

    /**
     * Retrieve a local claim.
     *
     * @param claimId claimId.
     * @return Local claim.
     */
    public LocalClaimResDTO getLocalClaim(String claimId) {

        try {
            List<LocalClaim> localClaimList = getClaimMetadataManagementService().getLocalClaims(
                    ContextLoader.getTenantDomainFromContext());

            LocalClaim localClaim = extractLocalClaimFromClaimList(base64DecodeId(claimId), localClaimList);

            if (localClaim == null) {
                throw handleClaimManagementClientError(ERROR_CODE_LOCAL_CLAIM_NOT_FOUND, NOT_FOUND, claimId);
            }

            return getLocalClaimResDTO(localClaim);

        } catch (ClaimMetadataException e) {
            throw handleClaimManagementException(e, ERROR_CODE_ERROR_RETRIEVING_LOCAL_CLAIM, claimId);
        }
    }

    /**
     * Retrieve all claims belonging to the local dialect.
     *
     * @param attributes attributes filter (optional).
     * @param limit      limit (optional).
     * @param offset     offset (optional).
     * @param filter     filter (optional).
     * @param sort       sort (optional).
     * @return List of local claims.
     */
    public List<LocalClaimResDTO> getLocalClaims(String attributes, Integer limit, Integer offset,
                                                 String filter, String sort) {

        handleNotImplementedCapabilities(attributes, limit, offset, filter, sort);

        try {
            List<LocalClaim> localClaimList = getClaimMetadataManagementService().getLocalClaims(
                    ContextLoader.getTenantDomainFromContext());

            return getLocalClaimResDTOs(localClaimList);

        } catch (ClaimMetadataException e) {
            throw handleClaimManagementException(e, ERROR_CODE_ERROR_RETRIEVING_LOCAL_CLAIMS);
        }
    }

    /**
     * Update a local claim.
     *
     * @param claimId          claimId.
     * @param localClaimReqDTO localClaimReqDTO.
     */
    public void updateLocalClaim(String claimId, LocalClaimReqDTO localClaimReqDTO) {

        if (!StringUtils.equals(base64DecodeId(claimId), localClaimReqDTO.getClaimURI())) {
            throw handleClaimManagementClientError(ERROR_CODE_LOCAL_CLAIM_CONFLICT, CONFLICT, base64DecodeId(claimId));
        }

        try {
            for (AttributeMappingDTO attributeMappingDTO :
                    localClaimReqDTO.getAttributeMapping()) {
                if (!isUserStoreExists(attributeMappingDTO.getUserstore())) {
                    throw handleClaimManagementClientError(ERROR_CODE_INVALID_USERSTORE, BAD_REQUEST,
                            attributeMappingDTO.getUserstore());
                }
            }

            getClaimMetadataManagementService().updateLocalClaim(
                    createLocalClaim(localClaimReqDTO),
                    ContextLoader.getTenantDomainFromContext());
        } catch (ClaimMetadataException e) {
            throw handleClaimManagementException(e, ERROR_CODE_ERROR_UPDATING_LOCAL_CLAIM, claimId);
        } catch (UserStoreException e) {
            throw handleException(e, ERROR_CODE_ERROR_ADDING_LOCAL_CLAIM, localClaimReqDTO.getClaimURI());
        }

        getResourceId(localClaimReqDTO.getClaimURI());
    }

    /**
     * Add an external claim.
     *
     * @param dialectId           dialectId.
     * @param externalClaimReqDTO externalClaimReqDTO.
     * @return Resource identifier.
     */
    public String addExternalClaim(String dialectId, ExternalClaimReqDTO externalClaimReqDTO) {

        try {
            if (!isDialectExists(dialectId)) {
                throw handleClaimManagementClientError(ERROR_CODE_INVALID_DIALECT_ID, BAD_REQUEST, dialectId);
            }

            getClaimMetadataManagementService().addExternalClaim(
                    createExternalClaim(dialectId, externalClaimReqDTO),
                    ContextLoader.getTenantDomainFromContext());
        } catch (ClaimMetadataException e) {
            throw handleClaimManagementException(e, ERROR_CODE_ERROR_ADDING_EXTERNAL_CLAIM,
                    externalClaimReqDTO.getClaimURI());
        }

        return getResourceId(externalClaimReqDTO.getClaimURI());
    }

    /**
     * Delete an external claim.
     *
     * @param dialectId dialectId.
     * @param claimId   claimId.
     */
    public void deleteExternalClaim(String dialectId, String claimId) {

        try {
            getClaimMetadataManagementService().removeExternalClaim(
                    base64DecodeId(dialectId),
                    base64DecodeId(claimId),
                    ContextLoader.getTenantDomainFromContext());
        } catch (ClaimMetadataException e) {
            throw handleClaimManagementException(e, ERROR_CODE_ERROR_DELETING_EXTERNAL_CLAIM, claimId);
        }

    }

    /**
     * Retrieve an external claim.
     *
     * @param dialectId dialectId.
     * @param claimId   claimId.
     * @return Local claim.
     */
    public ExternalClaimResDTO getExternalClaim(String dialectId, String claimId) {

        try {
            List<ExternalClaim> externalClaimList = getClaimMetadataManagementService().getExternalClaims(
                    base64DecodeId(dialectId),
                    ContextLoader.getTenantDomainFromContext());

            if (CollectionUtils.isEmpty(externalClaimList)) {
                throw handleClaimManagementClientError(ERROR_CODE_CLAIMS_NOT_FOUND_FOR_DIALECT, NOT_FOUND, dialectId);
            }

            ExternalClaim externalClaim = extractExternalClaimFromClaimList(base64DecodeId(claimId), externalClaimList);

            if (externalClaim == null) {
                throw handleClaimManagementClientError(ERROR_CODE_EXTERNAL_CLAIM_NOT_FOUND, NOT_FOUND, claimId,
                        dialectId);
            }

            return getExternalClaimResDTO(externalClaim);

        } catch (ClaimMetadataException e) {
            throw handleClaimManagementException(e, ERROR_CODE_ERROR_RETRIEVING_EXTERNAL_CLAIM, claimId, dialectId);
        }
    }

    /**
     * Retrieve all claims belonging to an external dialect.
     *
     * @param dialectId dialectId.
     * @param limit     limit (optional).
     * @param offset    offset (optional).
     * @param filter    filter (optional).
     * @param sort      sort (optional).
     * @return List of external claims.
     */
    public List<ExternalClaimResDTO> getExternalClaims(String dialectId, Integer limit, Integer offset,
                                                       String filter, String sort) {

        handleNotImplementedCapabilities(limit, offset, filter, sort);

        try {
            List<ExternalClaim> externalClaimList = getClaimMetadataManagementService().getExternalClaims(
                    base64DecodeId(dialectId),
                    ContextLoader.getTenantDomainFromContext());
            if (CollectionUtils.isEmpty(externalClaimList)) {
                throw handleClaimManagementClientError(ERROR_CODE_CLAIMS_NOT_FOUND_FOR_DIALECT, NOT_FOUND, dialectId);
            }

            return getExternalClaimResDTOs(externalClaimList);

        } catch (ClaimMetadataException e) {
            throw handleClaimManagementException(e, ERROR_CODE_ERROR_RETRIEVING_EXTERNAL_CLAIMS, dialectId);
        }
    }

    /**
     * Update an external claim.
     *
     * @param dialectId           dialectId.
     * @param claimId             claimId.
     * @param externalClaimReqDTO externalClaimReqDTO.
     */
    public void updateExternalClaim(String dialectId, String claimId, ExternalClaimReqDTO externalClaimReqDTO) {

        if (!StringUtils.equals(base64DecodeId(claimId), externalClaimReqDTO.getClaimURI())) {
            throw handleClaimManagementClientError(ERROR_CODE_EXTERNAL_CLAIM_CONFLICT, CONFLICT,
                    base64DecodeId(claimId), dialectId);
        }

        try {
            getClaimMetadataManagementService().updateExternalClaim(
                    createExternalClaim(dialectId, externalClaimReqDTO),
                    ContextLoader.getTenantDomainFromContext());
        } catch (ClaimMetadataException e) {
            throw handleClaimManagementException(e, ERROR_CODE_ERROR_UPDATING_EXTERNAL_CLAIM, claimId, dialectId);
        }

        getResourceId(externalClaimReqDTO.getClaimURI());
    }

    private ClaimDialect extractDialectFromDialectList(String dialectURI, List<ClaimDialect> dialectList) {

        for (ClaimDialect dialect : dialectList) {
            if (StringUtils.equals(dialect.getClaimDialectURI(), dialectURI)) {
                return dialect;
            }
        }

        return null;
    }

    private ExternalClaim extractExternalClaimFromClaimList(String claimURI, List<ExternalClaim> claimList) {

        for (ExternalClaim claim : claimList) {
            if (StringUtils.equals(claim.getClaimURI(), claimURI)) {
                return claim;
            }
        }

        return null;
    }

    private LocalClaim extractLocalClaimFromClaimList(String claimURI, List<LocalClaim> claimList) {

        for (LocalClaim claim : claimList) {
            if (StringUtils.equals(claim.getClaimURI(), claimURI)) {
                return claim;
            }
        }

        return null;
    }

    private ClaimDialect createClaimDialect(ClaimDialectReqDTO claimDialectReqDTO) {

        return new ClaimDialect(claimDialectReqDTO.getDialectURI());
    }

    private ClaimDialect createClaimDialect(String dialectURI) {

        return new ClaimDialect(dialectURI);
    }

    private ExternalClaim createExternalClaim(String dialectId, ExternalClaimReqDTO externalClaimReqDTO) {

        return new ExternalClaim(
                base64DecodeId(dialectId),
                externalClaimReqDTO.getClaimURI(),
                externalClaimReqDTO.getMappedLocalClaimURI());
    }

    private ClaimDialectResDTO getClaimDialectResDTO(ClaimDialect claimDialect) {

        ClaimDialectResDTO claimDialectResDTO = new ClaimDialectResDTO();

        String dialectId;
        if (StringUtils.equals(claimDialect.getClaimDialectURI(), LOCAL_DIALECT)) {
            dialectId = LOCAL_DIALECT_PATH;
        } else {
            dialectId = base64EncodeId(claimDialect.getClaimDialectURI());
        }
        claimDialectResDTO.setId(dialectId);
        claimDialectResDTO.setDialectURI(claimDialect.getClaimDialectURI());

        LinkDTO linkDTO = new LinkDTO();
        linkDTO.setRel(REL_CLAIMS);
        linkDTO.setHref(buildURI(String.format(V1_API_PATH_COMPONENT + CMT_PATH_COMPONENT + "/%s/claims", dialectId))
                .toString());

        claimDialectResDTO.setLink(linkDTO);

        return claimDialectResDTO;
    }

    private List<ClaimDialectResDTO> getClaimDialectResDTOs(List<ClaimDialect> claimDialectList) {

        List<ClaimDialectResDTO> claimDialectResDTOList = new ArrayList<>();

        for (ClaimDialect claimDialect : claimDialectList) {
            claimDialectResDTOList.add(getClaimDialectResDTO(claimDialect));
        }

        return claimDialectResDTOList;
    }

    private ExternalClaimResDTO getExternalClaimResDTO(ExternalClaim externalClaim) {

        ExternalClaimResDTO externalClaimResDTO = new ExternalClaimResDTO();

        externalClaimResDTO.setId(base64EncodeId(externalClaim.getClaimURI()));
        externalClaimResDTO.setClaimDialectURI(externalClaim.getClaimDialectURI());
        externalClaimResDTO.setClaimURI(externalClaim.getClaimURI());
        externalClaimResDTO.setMappedLocalClaimURI(externalClaim.getMappedLocalClaim());

        return externalClaimResDTO;
    }

    private List<ExternalClaimResDTO> getExternalClaimResDTOs(List<ExternalClaim> externalClaimList) {

        List<ExternalClaimResDTO> externalClaimResDTOList = new ArrayList<>();

        for (ExternalClaim externalClaim : externalClaimList) {
            externalClaimResDTOList.add(getExternalClaimResDTO(externalClaim));
        }

        return externalClaimResDTOList;
    }

    private LocalClaimResDTO getLocalClaimResDTO(LocalClaim localClaim) {

        LocalClaimResDTO localClaimResDTO = new LocalClaimResDTO();

        localClaimResDTO.setId(base64EncodeId(localClaim.getClaimURI()));
        localClaimResDTO.setClaimURI(localClaim.getClaimURI());
        localClaimResDTO.setDialectURI(localClaim.getClaimDialectURI());

        Map<String, String> claimProperties = new HashMap<>(localClaim.getClaimProperties());

        localClaimResDTO.setDescription(claimProperties.remove(PROP_DESCRIPTION));

        String propDisplayOrder = claimProperties.remove(PROP_DISPLAY_ORDER);
        if (StringUtils.isNumeric(propDisplayOrder)) {
            localClaimResDTO.setDisplayOrder(Integer.valueOf(propDisplayOrder));
        } else {
            localClaimResDTO.setDisplayOrder(0);
        }

        localClaimResDTO.setDisplayName(claimProperties.remove(PROP_DISPLAY_NAME));
        localClaimResDTO.setReadOnly(Boolean.valueOf(claimProperties.remove(PROP_READ_ONLY)));
        String regEx = claimProperties.remove(PROP_REG_EX);
        localClaimResDTO.setRegEx(regEx != null ? regEx : "");
        localClaimResDTO.setRequired(Boolean.valueOf(claimProperties.remove(PROP_REQUIRED)));
        localClaimResDTO.setSupportedByDefault(Boolean.valueOf(claimProperties.remove(PROP_SUPPORTED_BY_DEFAULT)));

        List<AttributeMappingDTO> attributeMappingDTOs = new ArrayList<>();
        for (AttributeMapping attributeMapping : localClaim.getMappedAttributes()) {
            AttributeMappingDTO attributeMappingDTO = new AttributeMappingDTO();

            attributeMappingDTO.setUserstore(attributeMapping.getUserStoreDomain());
            attributeMappingDTO.setMappedAttribute(attributeMapping.getAttributeName());

            attributeMappingDTOs.add(attributeMappingDTO);
        }
        localClaimResDTO.setAttributeMapping(attributeMappingDTOs);
        localClaimResDTO.setProperties(mapToProperties(claimProperties));

        return localClaimResDTO;
    }

    private List<LocalClaimResDTO> getLocalClaimResDTOs(List<LocalClaim> localClaimList) {

        List<LocalClaimResDTO> localClaimResDTOList = new ArrayList<>();

        for (LocalClaim localClaim : localClaimList) {
            localClaimResDTOList.add(getLocalClaimResDTO(localClaim));
        }

        return localClaimResDTOList;
    }

    private LocalClaim createLocalClaim(LocalClaimReqDTO localClaimReqDTO) {

        Map<String, String> claimProperties = new HashMap<>();

        if (StringUtils.isNotBlank(localClaimReqDTO.getDisplayName())) {
            claimProperties.put(PROP_DISPLAY_NAME, localClaimReqDTO.getDisplayName());
        }

        if (StringUtils.isNotBlank(localClaimReqDTO.getDescription())) {
            claimProperties.put(PROP_DESCRIPTION, localClaimReqDTO.getDescription());
        }

        if (StringUtils.isNotBlank(localClaimReqDTO.getRegEx())) {
            claimProperties.put(PROP_REG_EX, localClaimReqDTO.getRegEx());
        }

        claimProperties.put(PROP_DISPLAY_ORDER, String.valueOf(localClaimReqDTO.getDisplayOrder()));
        claimProperties.put(PROP_READ_ONLY, String.valueOf(localClaimReqDTO.getReadOnly()));
        claimProperties.put(PROP_REQUIRED, String.valueOf(localClaimReqDTO.getRequired()));
        claimProperties.put(PROP_SUPPORTED_BY_DEFAULT, String.valueOf(localClaimReqDTO.getSupportedByDefault()));
        claimProperties.putAll(propertiesToMap(localClaimReqDTO.getProperties()));

        List<AttributeMapping> attributeMappings = new ArrayList<>();
        for (AttributeMappingDTO attributeMappingDTO : localClaimReqDTO.getAttributeMapping()) {
            attributeMappings.add(new AttributeMapping(attributeMappingDTO.getUserstore(),
                    attributeMappingDTO.getMappedAttribute()));
        }

        return new LocalClaim(localClaimReqDTO.getClaimURI(), attributeMappings, claimProperties);
    }

    private String base64EncodeId(String id) {

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(id.getBytes(StandardCharsets.UTF_8));
    }

    private String base64DecodeId(String id) {

        return new String(
                Base64.getUrlDecoder().decode(id),
                StandardCharsets.UTF_8);
    }

    private String getResourceId(String val) {

        // TODO: Change implementation.
        return base64EncodeId(val);
    }

    private boolean isUserStoreExists(String userStoreDomain) throws UserStoreException {

        UserStoreManager userStoreManager;
        if (IdentityUtil.getPrimaryDomainName().equals(userStoreDomain)) {
            return true;
        } else {
            userStoreManager = ((UserStoreManager) CarbonContext.getThreadLocalCarbonContext().getUserRealm()
                    .getUserStoreManager()).getSecondaryUserStoreManager(userStoreDomain);
        }

        return userStoreManager != null;
    }

    private boolean isDialectExists(String dialectId) throws ClaimMetadataException {

        List<ClaimDialect> claimDialectList =
                getClaimMetadataManagementService().getClaimDialects(ContextLoader.getTenantDomainFromContext());
        ClaimDialect claimDialect = extractDialectFromDialectList(base64DecodeId(dialectId), claimDialectList);

        return claimDialect != null;
    }

    private APIError handleClaimManagementException(ClaimMetadataException e, Constant.ErrorMessage errorEnum,
                                                    String... data) {

        if (e instanceof ClaimMetadataClientException) {
            return handleClaimManagementClientError(Constant.ErrorMessage.getMappedErrorMessage(e.getErrorCode()),
                    BAD_REQUEST, data);
        }

        return handleException(e, errorEnum, data);
    }

    private APIError handleClaimManagementClientError(Constant.ErrorMessage errorEnum, Response.Status status,
                                                      String... data) {

        ErrorResponse errorResponse = getErrorBuilder(errorEnum, data)
                .build(LOG, buildErrorDescription(errorEnum, data));

        return new APIError(status, errorResponse);
    }

    private void handleNotImplementedCapabilities(Integer limit, Integer offset, String filter, String sort) {

        handleNotImplementedCapabilities(null, limit, offset, filter, sort);
    }

    private APIError handleException(Exception e, Constant.ErrorMessage errorEnum, String... data) {

        ErrorResponse errorResponse = getErrorBuilder(errorEnum, data)
                .build(LOG, e, buildErrorDescription(errorEnum, data));

        Response.Status status = Response.Status.INTERNAL_SERVER_ERROR;

        return new APIError(status, errorResponse);
    }

    private void handleNotImplementedCapabilities(String attributes, Integer limit, Integer offset, String filter,
                                                  String sort) {

        Constant.ErrorMessage errorEnum = null;

        if (limit != null) {
            errorEnum = ERROR_CODE_PAGINATION_NOT_IMPLEMENTED;
        } else if (offset != null) {
            errorEnum = ERROR_CODE_PAGINATION_NOT_IMPLEMENTED;
        } else if (filter != null) {
            errorEnum = ERROR_CODE_FILTERING_NOT_IMPLEMENTED;
        } else if (sort != null) {
            errorEnum = ERROR_CODE_SORTING_NOT_IMPLEMENTED;
        } else if (attributes != null) {
            errorEnum = ERROR_CODE_ATTRIBUTE_FILTERING_NOT_IMPLEMENTED;
        }

        if (errorEnum != null) {
            ErrorResponse errorResponse = getErrorBuilder(errorEnum).build(LOG, errorEnum.getDescription());
            Response.Status status = Response.Status.NOT_IMPLEMENTED;

            throw new APIError(status, errorResponse);
        }
    }

    private ErrorResponse.Builder getErrorBuilder(Constant.ErrorMessage errorEnum, String... data) {

        return new ErrorResponse.Builder()
                .withCode(errorEnum.getCode())
                .withMessage(errorEnum.getMessage())
                .withDescription(buildErrorDescription(errorEnum, data));
    }

    private String buildErrorDescription(Constant.ErrorMessage errorEnum, String... data) {

        String errorDescription;

        if (ArrayUtils.isNotEmpty(data)) {
            errorDescription = String.format(errorEnum.getDescription(), data);
        } else {
            errorDescription = errorEnum.getDescription();
        }

        return errorDescription;
    }

    private HashMap<String, String> propertiesToMap(List<PropertyDTO> propList) {

        HashMap<String, String> propMap = new HashMap<>();

        if (CollectionUtils.isNotEmpty(propList)) {
            for (PropertyDTO prop : propList) {
                propMap.put(prop.getKey(), prop.getValue());
            }
        }

        return propMap;
    }

    private List<PropertyDTO> mapToProperties(Map<String, String> propMap) {

        List<PropertyDTO> propList = new ArrayList<>();

        if (!propMap.isEmpty()) {
            propMap.forEach((k, v) -> {
                PropertyDTO propertyDTO = new PropertyDTO();

                propertyDTO.setKey(k);
                propertyDTO.setValue(v);

                propList.add(propertyDTO);
            });
        }
        return propList;
    }
}
