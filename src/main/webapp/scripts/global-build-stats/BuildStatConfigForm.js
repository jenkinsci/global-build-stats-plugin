/**
 * BuildStatConfigs class definition
 * Needs following things when included :
 * - BuildStatConfig.js file inclusion (and its dependencies)
 * - chartList.js file inclusion (and its dependencies)
 * - standardFunctions.js file inclusion (and its dependencies)
 * - CURRENT_FORM global constant
 * - CHART_LENGTH_UNIT global constant
 * - FIELD_FILTER_ALL, FIELD_FILTER_REGEX, LAUNCHER_SYSTEM_REGEX and NODE_MASTER_REGEX global constants
 */
class BuildStatConfigForm {
	constructor(buildStatConfId){
		if(buildStatConfId != null){
			this.buildStatConf = BUILD_STAT_CONFIGS.getBuildStat(buildStatConfId);
		} else {
			this.buildStatConf = null;
		}
		this.buildStatConfId = getBuildStatConfigId(buildStatConfId);
		CURRENT_FORM = this;
	}
	
	selectOption(selectElement, optionValue){
		for (var i=0; i<selectElement.options.length; i++) {
			selectElement.options[i].selected = selectElement.options[i].value == optionValue;
		}
		selectElement.onchange();
	}
	
	changeChartLengthUnit(newScale){
		for(var i=0; i<CHART_LENGTH_UNITS.length; i++){
		    if(newScale == CHART_LENGTH_UNITS[i]){
		    	document.getElementById(this.buildStatConfId+'_'+CHART_LENGTH_UNITS[i]).style.display = "inline";
		    } else {
		    	document.getElementById(this.buildStatConfId+'_'+CHART_LENGTH_UNITS[i]).style.display = "none";
		    }
		}
	}
	
	isModificationMode(){
		return this.buildStatConf != null;
	}
	
	// Called after buildStatConfigForm is displayed
	initForm(){
      if(this.buildStatConf!=null && this.buildStatConf.historicScale != '') {
      	this.selectOption(document.getElementById(this.buildStatConfId+'_historicScale'), this.buildStatConf.historicScale); 
      }
      if(this.buildStatConf!=null && this.buildStatConf.yAxisChartType!= '') {
      	this.selectOption(document.getElementById(this.buildStatConfId+'_yAxisChartType'), this.buildStatConf.yAxisChartType); 
      }
      
	  // Job filter default value
	  if(document.getElementById(this.buildStatConfId+'_jobFilter').value == ''){
	  	document.getElementById(this.buildStatConfId+'_jobFilter').value = FIELD_FILTER_ALL;
	  }
	  if(document.getElementById(this.buildStatConfId+'_jobFilter').value.indexOf(FIELD_FILTER_REGEX) != -1){
	  	  document.getElementById(this.buildStatConfId+'_jobFilteringType_REGEX').checked = 'checked';
	  	  initializeRegexField(this.buildStatConfId+'_jobNameRegex', this.buildStatConf.buildFilters.jobFilter);
	 	  document.getElementById(this.buildStatConfId+'_jobFilteringType_REGEX').onchange();
	  	  document.getElementById(this.buildStatConfId+'_jobNameRegex').disabled = false;
	  } else if(document.getElementById(this.buildStatConfId+'_jobFilter').value.indexOf(FIELD_FILTER_ALL) != -1){
	  	  document.getElementById(this.buildStatConfId+'_jobFilteringType_ALL').checked = 'checked';
	  	  document.getElementById(this.buildStatConfId+'_jobFilteringType_ALL').onchange();
	  }
	  
	  // Node filter default value
	  if(document.getElementById(this.buildStatConfId+'_nodeFilter').value == ''){
	  	document.getElementById(this.buildStatConfId+'_nodeFilter').value = FIELD_FILTER_ALL;
	  }
	  if(document.getElementById(this.buildStatConfId+'_nodeFilter').value.indexOf(NODE_MASTER_REGEX) != -1){
	  	  document.getElementById(this.buildStatConfId+'_nodeFilteringType_REGEXMASTER').checked = 'checked';
	 	  document.getElementById(this.buildStatConfId+'_nodeFilteringType_REGEXMASTER').onchange();
	  	  document.getElementById(this.buildStatConfId+'_nodeNameRegex').disabled = true;
	  } else if(document.getElementById(this.buildStatConfId+'_nodeFilter').value.indexOf(FIELD_FILTER_REGEX) != -1){
	  	  document.getElementById(this.buildStatConfId+'_nodeFilteringType_REGEX').checked = 'checked';
	  	  initializeRegexField(this.buildStatConfId+'_nodeNameRegex', this.buildStatConf.buildFilters.nodeFilter);
	 	  document.getElementById(this.buildStatConfId+'_nodeFilteringType_REGEX').onchange();
	  	  document.getElementById(this.buildStatConfId+'_nodeNameRegex').disabled = false;
	  } else if(document.getElementById(this.buildStatConfId+'_nodeFilter').value.indexOf(FIELD_FILTER_ALL) != -1){
	  	  document.getElementById(this.buildStatConfId+'_nodeFilteringType_ALL').checked = 'checked';
	  	  document.getElementById(this.buildStatConfId+'_nodeFilteringType_ALL').onchange();
	  }
	  
	  // Launcher filter default value
	  if(document.getElementById(this.buildStatConfId+'_launcherFilter').value == ''){
	  	document.getElementById(this.buildStatConfId+'_launcherFilter').value = FIELD_FILTER_ALL;
	  }
	  if(document.getElementById(this.buildStatConfId+'_launcherFilter').value.indexOf(LAUNCHER_SYSTEM_REGEX) != -1){
	  	  document.getElementById(this.buildStatConfId+'_launcherFilteringType_REGEXSYSTEM').checked = 'checked';
	 	  document.getElementById(this.buildStatConfId+'_launcherFilteringType_REGEXSYSTEM').onchange();
	  	  document.getElementById(this.buildStatConfId+'_launcherNameRegex').disabled = true;
	  } else if(document.getElementById(this.buildStatConfId+'_launcherFilter').value.indexOf(FIELD_FILTER_REGEX) != -1){
	  	  document.getElementById(this.buildStatConfId+'_launcherFilteringType_REGEX').checked = 'checked';
	  	  initializeRegexField(this.buildStatConfId+'_launcherNameRegex', this.buildStatConf.buildFilters.launcherFilter);
	 	  document.getElementById(this.buildStatConfId+'_launcherFilteringType_REGEX').onchange();
	  	  document.getElementById(this.buildStatConfId+'_launcherNameRegex').disabled = false;
	  } else if(document.getElementById(this.buildStatConfId+'_launcherFilter').value.indexOf(FIELD_FILTER_ALL) != -1){
	  	  document.getElementById(this.buildStatConfId+'_launcherFilteringType_ALL').checked = 'checked';
	  	  document.getElementById(this.buildStatConfId+'_launcherFilteringType_ALL').onchange();
	  }
	  
	  // Changing default values...
	  if(this.buildStatConf != null){
		  if(this.buildStatConf.buildFilters.successShown == false) { document.getElementById(this.buildStatConfId+'_successShown').checked = false; }
		  if(this.buildStatConf.buildFilters.failuresShown == false) { document.getElementById(this.buildStatConfId+'_failuresShown').checked = false; }
		  if(this.buildStatConf.buildFilters.unstablesShown == false) { document.getElementById(this.buildStatConfId+'_unstablesShown').checked = false; }
		  if(this.buildStatConf.buildFilters.abortedShown == false) { document.getElementById(this.buildStatConfId+'_abortedShown').checked = false; }
		  if(this.buildStatConf.buildFilters.notBuildsShown == true) { document.getElementById(this.buildStatConfId+'_notBuildsShown').checked = true; }
		  if(this.buildStatConf.buildStatusesShown == false) { document.getElementById(this.buildStatConfId+'_buildStatusesShown').checked = false; }
		  if(this.buildStatConf.totalBuildTimeShown == true) { document.getElementById(this.buildStatConfId+'_totalBuildTimeShown').checked = true; }
		  if(this.buildStatConf.averageBuildTimeShown == true) { document.getElementById(this.buildStatConfId+'_averageBuildTimeShown').checked = true; }
	  }
	  		  
	  // Initializing validations 
	  document.getElementById(this.buildStatConfId+'_title').onchange();
	  document.getElementById(this.buildStatConfId+'_buildStatWidth').onchange();
	  document.getElementById(this.buildStatConfId+'_buildStatHeight').onchange();
	  document.getElementById(this.buildStatConfId+'_historicScale').onchange();
	  document.getElementById(this.buildStatConfId+'_historicLength').onchange();
	}

	// Display a creation/modification form for build stat config
	displayBuildStatConfigForm(){
		var modificationMode = this.isModificationMode();
	
       	if(modificationMode){
       		var submitLabel = document.getElementById('updateSubmitLabel').innerHTML;
       		var popupTitle = document.getElementById('updatePopupTitle').innerHTML;
    		var bsId = this.buildStatConfId;
       	} else {
       		var submitLabel = document.getElementById('createSubmitLabel').innerHTML;
       		var popupTitle = document.getElementById('createPopupTitle').innerHTML;
    		var bsId = "new";
       	}
       	var overviewLabel = document.getElementById('overviewLabel').innerHTML;
       	var cancelLabel = document.getElementById('cancelLabel').innerHTML;
       	
		YAHOO.namespace("global.build.stat.configuration");
		YAHOO.global.build.stat.configuration._buttons = [];
	    YAHOO.global.build.stat.configuration.handleOverview = function() {
	    	// If error resides in form, don't do anything here !
	    	if(isDivErrorPresentInForm(document.getElementById('createBuildStat_'+bsId))){ return; }
	    	
			YAHOO.namespace("global.build.stat.overview");
			
			var title = document.getElementById(bsId+'_title').value;
			var width = document.getElementById(bsId+'_buildStatWidth').value;
			var height = document.getElementById(bsId+'_buildStatHeight').value;
			var scale = document.getElementById(bsId+'_historicScale').value;
			var length = document.getElementById(bsId+'_historicLength').value;
			var jobFilter = document.getElementById(bsId+'_jobFilter').value;
			var nodeFilter = document.getElementById(bsId+'_nodeFilter').value;
			var launcherFilter = document.getElementById(bsId+'_launcherFilter').value;
			var successShown = document.getElementById(bsId+'_successShown').checked;
			var failuresShown = document.getElementById(bsId+'_failuresShown').checked;
			var unstablesShown = document.getElementById(bsId+'_unstablesShown').checked;
			var abortedShown = document.getElementById(bsId+'_abortedShown').checked;
			var notBuildsShown = document.getElementById(bsId+'_notBuildsShown').checked;
			var buildStatusesShown = document.getElementById(bsId+'_buildStatusesShown').checked;
			var totalBuildTimeShown = document.getElementById(bsId+'_totalBuildTimeShown').checked;
			var averageBuildTimeShown = document.getElementById(bsId+'_averageBuildTimeShown').checked;
			var yAxisChartType = document.getElementById(bsId+'_yAxisChartType').value;

			var overviewContent = '<img src="'+rootURL+'/plugin/global-build-stats/createChart?' + new URLSearchParams({
				title: title,
				buildStatWidth: width,
				buildStatHeight: height,
				historicLength: length,
				historicScale: scale,
				jobFilter: jobFilter,
				nodeFilter: nodeFilter,
				launcherFilter: launcherFilter,
				successShown: successShown,
				failuresShown: failuresShown,
				unstablesShown: unstablesShown,
				abortedShown: abortedShown,
				notBuildsShown: notBuildsShown,
				yAxisChartType: yAxisChartType,
				buildStatusesShown: buildStatusesShown,
				totalBuildTimeShown: totalBuildTimeShown,
				averageBuildTimeShown: averageBuildTimeShown,
			}) + '" />';

			YAHOO.global.build.stat.overview.modalPopup =  
		        new YAHOO.widget.Panel("buildStatOverview",   
		            { width:width+"px",
		              fixedcenter:true,  
		              close:true,  
		              draggable:true,
		              zindex:8, 
		              modal:true
		            }
		        ); 
			YAHOO.global.build.stat.overview.modalPopup.setHeader(overviewLabel);
			YAHOO.global.build.stat.overview.modalPopup.setBody(overviewContent);
			YAHOO.global.build.stat.overview.modalPopup.render(document.body);
	    }
	    YAHOO.global.build.stat.configuration.handleSubmit = function() {
	    	// If error resides in form, don't do anything here !
	    	if(isDivErrorPresentInForm(document.getElementById('createBuildStat_'+bsId))){ return; }
	    	
			ajaxCall('form', 'createBuildStat_'+bsId, function(ret) {
			  	var buildStatConfig = eval('('+ret.responseText+')');
			  	if(modificationMode){
	    			BUILD_STAT_CONFIGS.update(bsId, buildStatConfig);
			  	} else {
		    		BUILD_STAT_CONFIGS.add(buildStatConfig);
			  	}
		        YAHOO.global.build.stat.configuration.modalPopup.hide();
		        CURRENT_FORM = null;
			});
	    } 
	    YAHOO.global.build.stat.configuration.handleCancel = function() { 
	        YAHOO.global.build.stat.configuration.modalPopup.hide(); 
	        CURRENT_FORM = null;
	    }
		YAHOO.global.build.stat.configuration.modalPopup =  
	        new YAHOO.widget.Panel("buildStatConfigForm",   
	            { width:"830px",
	              fixedcenter:true,  
	              close:false,  
	              draggable:false,  
	              zindex:4, 
	              modal:true
	            }
	        ); 
	        
		const content = this.getHTMLForBuildStatConfigForm();
		YAHOO.global.build.stat.configuration.modalPopup.setHeader(popupTitle);
		YAHOO.global.build.stat.configuration.modalPopup.setBody(content); 
	    YAHOO.global.build.stat.configuration.modalPopup.setFooter('<span id="panelFooter" class="button-group"></span>'); 
	    YAHOO.global.build.stat.configuration.modalPopup.showEvent.subscribe(function() { 
	        if (this._buttons.length == 0) { 
	            this._buttons[0] = new YAHOO.widget.Button({ 
	                type: 'button', 
	                label: overviewLabel, 
	                container: 'panelFooter' 
	            }); 
	            this._buttons[0].on('click', YAHOO.global.build.stat.configuration.handleOverview); 
	            this._buttons[1] = new YAHOO.widget.Button({ 
	                type: 'button', 
	                label: submitLabel, 
	                container: 'panelFooter' 
	            }); 
	            this._buttons[1].on('click', YAHOO.global.build.stat.configuration.handleSubmit); 
	            this._buttons[2] = new YAHOO.widget.Button({ 
	                type: 'button', 
	                label: cancelLabel, 
	                container: 'panelFooter' 
	            }); 
	            this._buttons[2].on('click', YAHOO.global.build.stat.configuration.handleCancel); 
	        }
	    }, YAHOO.global.build.stat.configuration, true); 
	    YAHOO.global.build.stat.configuration.modalPopup.renderEvent.subscribe(function() {
	    	CURRENT_FORM.initForm();
	    }, YAHOO.global.build.stat.configuration, true); 
		YAHOO.global.build.stat.configuration.modalPopup.render(document.body);
	}

	getHTMLForBuildStatConfigForm(){
		var currentContext = createTemplateContext(this.buildStatConf);
		
		if(this.isModificationMode()){
			var	regenerateIdBlockTemplate = getTemplateContent('regenerateIdBlockTemplate');
			var regenerateIdBlock = evaluateTemplate(regenerateIdBlockTemplate, currentContext);
		} else {
			var regenerateIdBlock = "";
		}
		
		currentContext = jsonConcat(currentContext, { regenerateIdBlock: regenerateIdBlock},
			{unsanitized: ['regenerateIdBlock']});
		
		// Generating content for creation/update form
		var formBlockTemplate = getTemplateContent('formBlockTemplate');
		var formBlock = evaluateTemplate(formBlockTemplate, currentContext);
		
		return formBlock;
	}
};
