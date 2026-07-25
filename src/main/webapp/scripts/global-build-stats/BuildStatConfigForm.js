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
    const event = new Event("change");
    selectElement.dispatchEvent(event);
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

      let event = new Event("change");

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
	  	  document.getElementById(this.buildStatConfId+'_jobFilteringType_ALL').dispatchEvent(event);
	  }
	  
	  // Node filter default value
	  if(document.getElementById(this.buildStatConfId+'_nodeFilter').value == ''){
	  	document.getElementById(this.buildStatConfId+'_nodeFilter').value = FIELD_FILTER_ALL;
	  }
	  if(document.getElementById(this.buildStatConfId+'_nodeFilter').value.indexOf(NODE_MASTER_REGEX) != -1){
	  	  document.getElementById(this.buildStatConfId+'_nodeFilteringType_REGEXMASTER').checked = 'checked';
	 	  document.getElementById(this.buildStatConfId+'_nodeFilteringType_REGEXMASTER').dispatchEvent(event);
	  	  document.getElementById(this.buildStatConfId+'_nodeNameRegex').disabled = true;
	  } else if(document.getElementById(this.buildStatConfId+'_nodeFilter').value.indexOf(FIELD_FILTER_REGEX) != -1){
	  	  document.getElementById(this.buildStatConfId+'_nodeFilteringType_REGEX').checked = 'checked';
	  	  initializeRegexField(this.buildStatConfId+'_nodeNameRegex', this.buildStatConf.buildFilters.nodeFilter);
	 	  document.getElementById(this.buildStatConfId+'_nodeFilteringType_REGEX').dispatchEvent(event);
	  	  document.getElementById(this.buildStatConfId+'_nodeNameRegex').disabled = false;
	  } else if(document.getElementById(this.buildStatConfId+'_nodeFilter').value.indexOf(FIELD_FILTER_ALL) != -1){
	  	  document.getElementById(this.buildStatConfId+'_nodeFilteringType_ALL').checked = 'checked';
	  	  document.getElementById(this.buildStatConfId+'_nodeFilteringType_ALL').dispatchEvent(event);
	  }
	  
	  // Launcher filter default value
	  if(document.getElementById(this.buildStatConfId+'_launcherFilter').value == ''){
	  	document.getElementById(this.buildStatConfId+'_launcherFilter').value = FIELD_FILTER_ALL;
	  }
	  if(document.getElementById(this.buildStatConfId+'_launcherFilter').value.indexOf(LAUNCHER_SYSTEM_REGEX) != -1){
	  	  document.getElementById(this.buildStatConfId+'_launcherFilteringType_REGEXSYSTEM').checked = 'checked';
	 	  document.getElementById(this.buildStatConfId+'_launcherFilteringType_REGEXSYSTEM').dispatchEvent(event);
	  	  document.getElementById(this.buildStatConfId+'_launcherNameRegex').disabled = true;
	  } else if(document.getElementById(this.buildStatConfId+'_launcherFilter').value.indexOf(FIELD_FILTER_REGEX) != -1){
	  	  document.getElementById(this.buildStatConfId+'_launcherFilteringType_REGEX').checked = 'checked';
	  	  initializeRegexField(this.buildStatConfId+'_launcherNameRegex', this.buildStatConf.buildFilters.launcherFilter);
	 	  document.getElementById(this.buildStatConfId+'_launcherFilteringType_REGEX').dispatchEvent(event);
	  	  document.getElementById(this.buildStatConfId+'_launcherNameRegex').disabled = false;
	  } else if(document.getElementById(this.buildStatConfId+'_launcherFilter').value.indexOf(FIELD_FILTER_ALL) != -1){
	  	  document.getElementById(this.buildStatConfId+'_launcherFilteringType_ALL').checked = 'checked';
	  	  document.getElementById(this.buildStatConfId+'_launcherFilteringType_ALL').dispatchEvent(event);
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
    document.getElementById(this.buildStatConfId+'_title').dispatchEvent(event);
    document.getElementById(this.buildStatConfId+'_buildStatWidth').dispatchEvent(event);
    document.getElementById(this.buildStatConfId+'_buildStatHeight').dispatchEvent(event);
    document.getElementById(this.buildStatConfId+'_historicScale').dispatchEvent(event);
    document.getElementById(this.buildStatConfId+'_historicLength').dispatchEvent(event);
	}

	// Display a creation/modification form for build stat config
	displayBuildStatConfigForm(){
		var modificationMode = this.isModificationMode();
	
       	if(modificationMode){
       		var submitLabel = document.getElementById('updateSubmitLabel').innerHTML;
       		var popupTitle = document.getElementById('updatePopupTitle').innerHTML;
          var submitMessage = document.getElementById('updateMessage').innerHTML;
    		var bsId = this.buildStatConfId;
       	} else {
       		var submitLabel = document.getElementById('createSubmitLabel').innerHTML;
       		var popupTitle = document.getElementById('createPopupTitle').innerHTML;
          var submitMessage = document.getElementById('createMessage').innerHTML;
    		var bsId = "new";
       	}
       	var overviewLabel = document.getElementById('overviewLabel').innerHTML;
       	var cancelLabel = document.getElementById('cancelLabel').innerHTML;
       	
	    let handleOverview = function() {
	    	// If error resides in form, don't do anything here !
	    	if(isDivErrorPresentInForm(document.getElementById('createBuildStat_'+bsId))){ return; }
	    	

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

        const previewArea = document.createElement("div");
        previewArea.innerHTML = overviewContent;
        dialog.modal(previewArea, {
          title: "Preview",
          maxWidth: width
        });
      }


		const content = createElementFromHtml(this.getHTMLForBuildStatConfigForm());
    const previewButton = content.querySelector(".gbs-preview-button");
    previewButton.addEventListener("click", function() {
      handleOverview();
    });
    const oldDisplay = content.style.display;
    content.style.display = "none";
    document.body.append(content);
    Behaviour.applySubtree(content);
    CURRENT_FORM.initForm();
    content.remove();
    content.style.display = oldDisplay;

    dialog.form(content, {
      title: popupTitle,
      okText: submitLabel,
      cancelText: cancelLabel,
      submitButton: false,
    }).then(formData => {
      let ajaxCallParams = createAjaxCallParams(function(ret) {
        const buildStatConfig = JSON.parse(ret.responseText);
        if(modificationMode){
          BUILD_STAT_CONFIGS.update(bsId, buildStatConfig);
        } else {
          BUILD_STAT_CONFIGS.add(buildStatConfig);
        }
        notificationBar.show(submitMessage, notificationBar.SUCCESS);
      });
      ajaxRequest(content.action, ajaxCallParams, new URLSearchParams(formData).toString());
      CURRENT_FORM = null;
    }, () => {
      CURRENT_FORM = null;
    });
	}

	getHTMLForBuildStatConfigForm(){
		var currentContext = createTemplateContext(this.buildStatConf);
		
		if(this.isModificationMode()){
			var	regenerateIdBlockTemplate = getTemplateContent('regenerateIdBlockTemplate');
			var regenerateIdBlock = evaluateTemplate(regenerateIdBlockTemplate, currentContext);
		} else {
			var regenerateIdBlock = "";
		}
		
		currentContext = jsonConcat(currentContext, { regenerateIdBlock: regenerateIdBlock,
			unsanitized: ['regenerateIdBlock']});
		
		// Generating content for creation/update form
		var formBlockTemplate = getTemplateContent('formBlockTemplate');
		var formBlock = evaluateTemplate(formBlockTemplate, currentContext);
		
		return formBlock;
	}
};

window.newBuildStatConfigForm = function() {
  new BuildStatConfigForm(null).displayBuildStatConfigForm();
}


Behaviour.specify(".gbs-newBuildStatConfigForm", "gbs-newBuildStatConfigForm", 0, function(element) {
  element.addEventListener("click", (e) => {
    newBuildStatConfigForm();
  })
});