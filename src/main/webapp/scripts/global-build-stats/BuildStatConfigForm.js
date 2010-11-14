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
var BuildStatConfigForm = Class.create();
BuildStatConfigForm.prototype = {
	initialize: function(buildStatConfId){
		if(buildStatConfId != null){
			this.buildStatConf = BUILD_STAT_CONFIGS.getBuildStat(buildStatConfId);
		} else {
			this.buildStatConf = null;
		}
		this.buildStatConfId = getBuildStatConfigId(buildStatConfId);
		CURRENT_FORM = this;
	},
	
	selectOption: function(selectElement, optionValue){
		for (var i=0; i<selectElement.options.length; i++) {
			selectElement.options[i].selected = selectElement.options[i].value == optionValue;
		}
		selectElement.onchange();
	},
	
	changeChartLengthUnit: function(newScale){
		for(var i=0; i<CHART_LENGTH_UNITS.length; i++){
		    if(newScale == CHART_LENGTH_UNITS[i]){
		    	document.getElementById(this.buildStatConfId+'_'+CHART_LENGTH_UNITS[i]).style.display = "inline";
		    } else {
		    	document.getElementById(this.buildStatConfId+'_'+CHART_LENGTH_UNITS[i]).style.display = "none";
		    }
		}
	},
	
	isModificationMode: function(){
		return this.buildStatConf != null;
	},
	
	// Called after buildStatConfigForm is displayed
	initForm: function(){
      if(this.buildStatConf!=null && this.buildStatConf.historicScale != '') {
      	this.selectOption($(this.buildStatConfId+'_historicScale'), this.buildStatConf.historicScale); 
      }
      if(this.buildStatConf!=null && this.buildStatConf.yAxisChartType!= '') {
      	this.selectOption($(this.buildStatConfId+'_yAxisChartType'), this.buildStatConf.yAxisChartType); 
      }
      
	  // Job filter default value
	  if($(this.buildStatConfId+'_jobFilter').value == ''){
	  	$(this.buildStatConfId+'_jobFilter').value = FIELD_FILTER_ALL;
	  }
	  if($(this.buildStatConfId+'_jobFilter').value.indexOf(FIELD_FILTER_REGEX) != -1){
	  	  $(this.buildStatConfId+'_jobFilteringType_REGEX').checked = 'checked';
	  	  initializeRegexField(this.buildStatConfId+'_jobNameRegex', this.buildStatConf.buildFilters.jobFilter);
	 	  $(this.buildStatConfId+'_jobFilteringType_REGEX').onchange();
	  	  $(this.buildStatConfId+'_jobNameRegex').disabled = false;
	  } else if($(this.buildStatConfId+'_jobFilter').value.indexOf(FIELD_FILTER_ALL) != -1){
	  	  $(this.buildStatConfId+'_jobFilteringType_ALL').checked = 'checked';
	  	  $(this.buildStatConfId+'_jobFilteringType_ALL').onchange();
	  }
	  
	  // Node filter default value
	  if($(this.buildStatConfId+'_nodeFilter').value == ''){
	  	$(this.buildStatConfId+'_nodeFilter').value = FIELD_FILTER_ALL;
	  }
	  if($(this.buildStatConfId+'_nodeFilter').value.indexOf(NODE_MASTER_REGEX) != -1){
	  	  $(this.buildStatConfId+'_nodeFilteringType_REGEXMASTER').checked = 'checked';
	 	  $(this.buildStatConfId+'_nodeFilteringType_REGEXMASTER').onchange();
	  	  $(this.buildStatConfId+'_nodeNameRegex').disabled = true;
	  } else if($(this.buildStatConfId+'_nodeFilter').value.indexOf(FIELD_FILTER_REGEX) != -1){
	  	  $(this.buildStatConfId+'_nodeFilteringType_REGEX').checked = 'checked';
	  	  initializeRegexField(this.buildStatConfId+'_nodeNameRegex', this.buildStatConf.buildFilters.nodeFilter);
	 	  $(this.buildStatConfId+'_nodeFilteringType_REGEX').onchange();
	  	  $(this.buildStatConfId+'_nodeNameRegex').disabled = false;
	  } else if($(this.buildStatConfId+'_nodeFilter').value.indexOf(FIELD_FILTER_ALL) != -1){
	  	  $(this.buildStatConfId+'_nodeFilteringType_ALL').checked = 'checked';
	  	  $(this.buildStatConfId+'_nodeFilteringType_ALL').onchange();
	  }
	  
	  // Launcher filter default value
	  if($(this.buildStatConfId+'_launcherFilter').value == ''){
	  	$(this.buildStatConfId+'_launcherFilter').value = FIELD_FILTER_ALL;
	  }
	  if($(this.buildStatConfId+'_launcherFilter').value.indexOf(LAUNCHER_SYSTEM_REGEX) != -1){
	  	  $(this.buildStatConfId+'_launcherFilteringType_REGEXSYSTEM').checked = 'checked';
	 	  $(this.buildStatConfId+'_launcherFilteringType_REGEXSYSTEM').onchange();
	  	  $(this.buildStatConfId+'_launcherNameRegex').disabled = true;
	  } else if($(this.buildStatConfId+'_launcherFilter').value.indexOf(FIELD_FILTER_REGEX) != -1){
	  	  $(this.buildStatConfId+'_launcherFilteringType_REGEX').checked = 'checked';
	  	  initializeRegexField(this.buildStatConfId+'_launcherNameRegex', this.buildStatConf.buildFilters.launcherFilter);
	 	  $(this.buildStatConfId+'_launcherFilteringType_REGEX').onchange();
	  	  $(this.buildStatConfId+'_launcherNameRegex').disabled = false;
	  } else if($(this.buildStatConfId+'_launcherFilter').value.indexOf(FIELD_FILTER_ALL) != -1){
	  	  $(this.buildStatConfId+'_launcherFilteringType_ALL').checked = 'checked';
	  	  $(this.buildStatConfId+'_launcherFilteringType_ALL').onchange();
	  }
	  
	  // Changing default values...
	  if(this.buildStatConf != null){
		  if(this.buildStatConf.buildFilters.successShown == false) { $(this.buildStatConfId+'_successShown').checked = false; }
		  if(this.buildStatConf.buildFilters.failuresShown == false) { $(this.buildStatConfId+'_failuresShown').checked = false; }
		  if(this.buildStatConf.buildFilters.unstablesShown == false) { $(this.buildStatConfId+'_unstablesShown').checked = false; }
		  if(this.buildStatConf.buildFilters.abortedShown == false) { $(this.buildStatConfId+'_abortedShown').checked = false; }
		  if(this.buildStatConf.buildFilters.notBuildsShown == true) { $(this.buildStatConfId+'_notBuildsShown').checked = true; }
		  if(this.buildStatConf.buildStatusesShown == false) { $(this.buildStatConfId+'_buildStatusesShown').checked = false; }
		  if(this.buildStatConf.totalBuildTimeShown == true) { $(this.buildStatConfId+'_totalBuildTimeShown').checked = true; }
		  if(this.buildStatConf.averageBuildTimeShown == true) { $(this.buildStatConfId+'_averageBuildTimeShown').checked = true; }
	  }
	  		  
	  // Initializing validations 
	  $(this.buildStatConfId+'_title').onchange();
	  $(this.buildStatConfId+'_buildStatWidth').onchange();
	  $(this.buildStatConfId+'_buildStatHeight').onchange();
	  $(this.buildStatConfId+'_historicScale').onchange();
	  $(this.buildStatConfId+'_historicLength').onchange();
	},

	// Display a creation/modification form for build stat config
	displayBuildStatConfigForm: function(){
		var modificationMode = this.isModificationMode();
	
       	if(modificationMode){
       		var submitLabel = $('updateSubmitLabel').innerHTML;
       		var popupTitle = $('updatePopupTitle').innerHTML;
    		var bsId = this.buildStatConfId;
       	} else {
       		var submitLabel = $('createSubmitLabel').innerHTML;
       		var popupTitle = $('createPopupTitle').innerHTML;
    		var bsId = "new";
       	}
       	var overviewLabel = $('overviewLabel').innerHTML;
       	var cancelLabel = $('cancelLabel').innerHTML;
       	
		YAHOO.namespace("global.build.stat.configuration");
		YAHOO.global.build.stat.configuration._buttons = [];
	    YAHOO.global.build.stat.configuration.handleOverview = function() {
	    	// If error resides in form, don't do anything here !
	    	if(isDivErrorPresentInForm($('createBuildStat_'+bsId))){ return; }
	    	
			YAHOO.namespace("global.build.stat.overview");
			
			var title = $(bsId+'_title').value;
			var width = $(bsId+'_buildStatWidth').value;
			var height = $(bsId+'_buildStatHeight').value;
			var scale = $(bsId+'_historicScale').value;
			var length = $(bsId+'_historicLength').value;
			var jobFilter = $(bsId+'_jobFilter').value;
			var nodeFilter = $(bsId+'_nodeFilter').value;
			var launcherFilter = $(bsId+'_launcherFilter').value;
			var successShown = $(bsId+'_successShown').checked;
			var failuresShown = $(bsId+'_failuresShown').checked;
			var unstablesShown = $(bsId+'_unstablesShown').checked;
			var abortedShown = $(bsId+'_abortedShown').checked;
			var notBuildsShown = $(bsId+'_notBuildsShown').checked;
			var buildStatusesShown = $(bsId+'_buildStatusesShown').checked;
			var totalBuildTimeShown = $(bsId+'_totalBuildTimeShown').checked;
			var averageBuildTimeShown = $(bsId+'_averageBuildTimeShown').checked;
			var yAxisChartType = $(bsId+'_yAxisChartType').value;

			var overviewContent = '<img src="'+rootURL+'/plugin/global-build-stats/createChart?';
			overviewContent += 'title='+title;
			overviewContent += '&buildStatWidth='+width;
			overviewContent += '&buildStatHeight='+height;
			overviewContent += '&historicLength='+length;
			overviewContent += '&historicScale='+scale;
			overviewContent += '&jobFilter='+jobFilter;
			overviewContent += '&nodeFilter='+nodeFilter;
			overviewContent += '&launcherFilter='+launcherFilter;
			overviewContent += '&successShown='+successShown;
			overviewContent += '&failuresShown='+failuresShown;
			overviewContent += '&unstablesShown='+unstablesShown;
			overviewContent += '&abortedShown='+abortedShown;
			overviewContent += '&notBuildsShown='+notBuildsShown;
			overviewContent += '&yAxisChartType='+yAxisChartType;
			overviewContent += '&buildStatusesShown='+buildStatusesShown;
			overviewContent += '&totalBuildTimeShown='+totalBuildTimeShown;
			overviewContent += '&averageBuildTimeShown='+averageBuildTimeShown;
			overviewContent += '" />';

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
	    	if(isDivErrorPresentInForm($('createBuildStat_'+bsId))){ return; }
	    	
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
	        
		content = this.getHTMLForBuildStatConfigForm();
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
	},

	getHTMLForBuildStatConfigForm: function(){
		var currentContext = createTemplateContext(this.buildStatConf);
		
		if(this.isModificationMode()){
			var	regenerateIdBlockTemplate = new Template(getTemplateContent('regenerateIdBlockTemplate'));
			var regenerateIdBlock = regenerateIdBlockTemplate.evaluate(currentContext);
		} else {
			var regenerateIdBlock = "";
		}
		
		currentContext = jsonConcat(currentContext, { regenerateIdBlock: regenerateIdBlock});
		
		// Generating content for creation/update form
		var formBlockTemplate = new Template(getTemplateContent('formBlockTemplate'));
		var formBlock = formBlockTemplate.evaluate(currentContext);
		
		return formBlock;
	}
};
