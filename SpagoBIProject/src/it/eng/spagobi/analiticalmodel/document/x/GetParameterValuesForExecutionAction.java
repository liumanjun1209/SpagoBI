/**

SpagoBI - The Business Intelligence Free Platform

Copyright (C) 2005-2009 Engineering Ingegneria Informatica S.p.A.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

**/
package it.eng.spagobi.analiticalmodel.document.x;


import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanAttribute;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.handlers.LovResultCacheManager;
import it.eng.spagobi.analiticalmodel.document.handlers.ExecutionInstance;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.ObjParuse;
import it.eng.spagobi.behaviouralmodel.lov.bo.DependenciesPostProcessingLov;
import it.eng.spagobi.behaviouralmodel.lov.bo.ILovDetail;
import it.eng.spagobi.behaviouralmodel.lov.bo.LovResultHandler;
import it.eng.spagobi.chiron.serializer.JSONStoreFeedTransformer;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.services.DelegatedBasicListService;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.cache.CacheInterface;
import it.eng.spagobi.utilities.cache.CacheSingleton;
import it.eng.spagobi.utilities.exceptions.SpagoBIServiceException;
import it.eng.spagobi.utilities.service.JSONSuccess;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */
public class GetParameterValuesForExecutionAction  extends AbstractSpagoBIAction {
	
	public static final String SERVICE_NAME = "GET_PARAMETERS_FOR_EXECUTION_SERVICE";
	
	// request parameters
	public static String PARAMETER_ID = "PARAMETER_ID";
	public static String SELECTED_PARAMETER_VALUES = "PARAMETERS";
	public static String FILTERS = "FILTERS";	
	public static String MODE = "MODE";
	public static String MODE_SIMPLE = "simple";
	public static String MODE_COMPLETE = "complete";
	public static String START = "start";
	public static String LIMIT = "limit";
	
	
	// logger component
	private static Logger logger = Logger.getLogger(GetParameterValuesForExecutionAction.class);
	
	
	public void doService() {
		
		String biparameterId;
		JSONObject selectedParameterValuesJSON;
		JSONObject filtersJSON = null;
		Map selectedParameterValues;
		String mode;
		Integer start;
		Integer limit;
		BIObjectParameter biObjectParameter;
		ExecutionInstance executionInstance;
		String valueColumn;
		String displayColumn;
		String descriptionColumn;
		List rows;
		List<ObjParuse> biParameterExecDependencies;
		ILovDetail lovProvDet;
		CacheInterface cache;
		
		
		logger.debug("IN");
		
		try {
		
			biparameterId = getAttributeAsString( PARAMETER_ID );
			selectedParameterValuesJSON = getAttributeAsJSONObject( SELECTED_PARAMETER_VALUES );
			if(this.requestContainsAttribute( FILTERS ) ) {
				filtersJSON = getAttributeAsJSONObject( FILTERS );
			}
			
			mode = getAttributeAsString( MODE );
			start = getAttributeAsInteger( START );
			limit = getAttributeAsInteger( LIMIT );
			
			logger.debug("Parameter [" + PARAMETER_ID + "] is equals to [" + biparameterId + "]");
			logger.debug("Parameter [" + MODE + "] is equals to [" + mode + "]");
			logger.debug("Parameter [" + START + "] is equals to [" + start + "]");
			logger.debug("Parameter [" + LIMIT + "] is equals to [" + limit + "]");
			
			if(mode == null) {
				mode = MODE_SIMPLE;
			}
			
			Assert.assertNotNull(getContext(), "Parameter [" + PARAMETER_ID + "] cannot be null" );
			Assert.assertNotNull(getContext(), "Execution context cannot be null" );
			Assert.assertNotNull(getContext().getExecutionInstance( ExecutionInstance.class.getName() ), "Execution instance cannot be null");
		
			executionInstance = getContext().getExecutionInstance( ExecutionInstance.class.getName() );
			executionInstance.refreshParametersValues(selectedParameterValuesJSON, false);
			
			BIObject obj = executionInstance.getBIObject();
			
			// START converts JSON object with document's parameters into an hashmap
			selectedParameterValues = null;
			if(selectedParameterValuesJSON != null) {
				try {
					selectedParameterValues = new HashMap();
					Iterator it = selectedParameterValuesJSON.keys();
					while(it.hasNext()){
						String key = (String)it.next();
						Object v = selectedParameterValuesJSON.get(key);
						if(v instanceof JSONArray) {
							JSONArray a = (JSONArray)v;
							String[] nv = new String[a.length()];
							for(int i = 0; i < a.length(); i++) {
									nv[i] = (String)a.get(i);
							}
							
							selectedParameterValues.put( key, nv );
					} else if(v instanceof String) {
							selectedParameterValues.put( key, (String)v );
						} else {
							Assert.assertUnreachable("attributes of PARAMETERS JSONObject can be only JSONArray or String");
						}
					}
				} catch (JSONException e) {
					throw new SpagoBIServiceException("parameter JSONObject is malformed", e);
				}
			}
			// END converts JSON object with document's parameters into an hashmap
			
			// START get the relevant biobject parameter 
			biObjectParameter = null;
			List parameters = obj.getBiObjectParameters();
			for(int i = 0; i < parameters.size(); i++) {
				BIObjectParameter p = (BIObjectParameter) parameters.get(i);
				if( biparameterId.equalsIgnoreCase( p.getParameterUrlName() ) ) {
					biObjectParameter = p;
					break;
				}
			}
			Assert.assertNotNull(biObjectParameter, "Impossible to find parameter [" + biparameterId + "]" );
			// END get the relevant biobject parameter 
			
			lovProvDet = executionInstance.getLovDetail(biObjectParameter);
			
			// START get the lov result
			String lovResult = null;
			try {
				// get the result of the lov
				IEngUserProfile profile = getUserProfile();

				// get from cache, if available
				LovResultCacheManager executionCacheManager = new LovResultCacheManager();
				lovResult = executionCacheManager.getLovResult(profile, biObjectParameter, executionInstance, true);
				
				// get all the rows of the result
				LovResultHandler lovResultHandler = new LovResultHandler(lovResult);		
				rows = lovResultHandler.getRows();
			
			} catch (Exception e) {
				throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to get parameter's values", e);
			}
			
			Assert.assertNotNull(lovResult, "Impossible to get parameter's values" );
			// END get the lov result
			
			// START filtering the list by filtering toolbar
			try {
				if(filtersJSON != null) {
					String valuefilter = (String) filtersJSON.get(SpagoBIConstants.VALUE_FILTER);
					String columnfilter = (String) filtersJSON.get(SpagoBIConstants.COLUMN_FILTER);
					String typeFilter = (String) filtersJSON.get(SpagoBIConstants.TYPE_FILTER);
					String typeValueFilter = (String) filtersJSON.get(SpagoBIConstants.TYPE_VALUE_FILTER);
					rows = DelegatedBasicListService.filterList(rows, valuefilter, typeValueFilter, columnfilter, typeFilter);
				}
			} catch (JSONException e) {
				throw new SpagoBIServiceException(SERVICE_NAME, "Impossible to read filter's configuration", e);
			}
			// END filtering the list by filtering toolbar
			
			
			// START filtering for correlation (only for DependenciesPostProcessingLov, i.e. scripts, java classes and fixed lists)
			biParameterExecDependencies = executionInstance
					.getDependencies(biObjectParameter);
			if (lovProvDet instanceof DependenciesPostProcessingLov
					&& selectedParameterValues != null
					&& biParameterExecDependencies != null
					&& biParameterExecDependencies.size() > 0) {
				rows = ((DependenciesPostProcessingLov) lovProvDet)
						.processDependencies(rows, selectedParameterValues,
								biParameterExecDependencies);
			}
			// END filtering for correlation
			
			
			// START building JSON object to be returned
			JSONObject valuesJSON;
			try {
				JSONArray valuesDataJSON = new JSONArray();
				
				valueColumn = lovProvDet.getValueColumnName();
				displayColumn = lovProvDet.getDescriptionColumnName();
				descriptionColumn = displayColumn;
				
				
				
				int lb = (start != null)? start.intValue(): 0;
				int ub = (limit != null)? lb + limit.intValue(): rows.size() - lb;
				ub = (ub > rows.size())? rows.size(): ub;
				
				for (int q = lb; q < ub; q++) {
					SourceBean row = (SourceBean) rows.get(q);
					JSONObject valueJSON = new JSONObject();
					
					if(MODE_COMPLETE.equalsIgnoreCase( mode )) {
						List columns = row.getContainedAttributes();
						for(int i = 0; i < columns.size(); i++) {
							SourceBeanAttribute attribute = (SourceBeanAttribute)columns.get(i);						
							valueJSON.put(attribute.getKey().toUpperCase(), attribute.getValue());
						}
					} else {
						String value = (String) row.getAttribute(valueColumn);
						String description = (String) row.getAttribute(descriptionColumn);					
						valueJSON.put("value", value);
						valueJSON.put("label", description);
						valueJSON.put("description", description);	
					}					
					
					valuesDataJSON.put(valueJSON);
				}
				
				String[] visiblecolumns;
				
				if(MODE_COMPLETE.equalsIgnoreCase( mode )) {
					visiblecolumns = (String[])lovProvDet.getVisibleColumnNames().toArray(new String[0]);
					for(int j = 0; j< visiblecolumns.length; j++) {
						visiblecolumns[j] = visiblecolumns[j].toUpperCase();
					}
				} else {
					
					valueColumn = "value";
					displayColumn = "label";
					descriptionColumn = "description";
					
					visiblecolumns = new String[]{"value", "label", "description"};
				}
				
				valuesJSON = (JSONObject)JSONStoreFeedTransformer.getInstance().transform(valuesDataJSON, 
						valueColumn.toUpperCase(), displayColumn.toUpperCase(), descriptionColumn.toUpperCase(), visiblecolumns, new Integer(rows.size()));
			} catch (Exception e) {
				throw new SpagoBIServiceException("Impossible to serialize response", e);
			}
			// END building JSON object to be returned
			
			
			try {
				writeBackToClient( new JSONSuccess( valuesJSON ) );
			} catch (IOException e) {
				throw new SpagoBIServiceException("Impossible to write back the responce to the client", e);
			}
		
		} finally {
			logger.debug("OUT");
		}		

	}
	
}
