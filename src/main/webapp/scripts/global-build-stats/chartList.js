function createTemplateContext(buildStatConfiguration){
	if(buildStatConfiguration==null || buildStatConfiguration.length == 0){
		// Creating context for creation
		var currentContext = {
			rootURL: rootURL, 
			formAction: "addBuildStatConfiguration", 
			buildStatId: getBuildStatConfigId(null),
			currentTime: new Date().getTime(),
			BUILD_STAT_CONTAINER_ID_PREFIX: BUILD_STAT_CONTAINER_ID_PREFIX,
			CDATAEndAndScript: "]]"+">"+"</"+"script"+">"
		};
	} else {
		// Creating context for update
		var currentContext = jsonConcat(buildStatConfiguration, {
			jobFilter: buildStatConfiguration.buildFilters.jobFilter,
			nodeFilter: buildStatConfiguration.buildFilters.nodeFilter,
			launcherFilter: buildStatConfiguration.buildFilters.launcherFilter,
			rootURL: rootURL, 
			formAction: "updateBuildStatConfiguration", 
			buildStatId: getBuildStatConfigId(buildStatConfiguration.id),
			currentTime: new Date().getTime(),
			BUILD_STAT_CONTAINER_ID_PREFIX: BUILD_STAT_CONTAINER_ID_PREFIX,
			CDATAEndAndScript: "]]"+">"+"</"+"script"+">"
		});
	}
	
	return currentContext;
}

function getBuildStatConfigId(buildStatConfigId){
	if(buildStatConfigId == null){
		return "new";
	} else {
		return buildStatConfigId;
	}
}

function jsonConcat(o1, o2) {
	for (var key in o2) {
		o1[key] = o2[key];
	}
	return o1;
}

// For some unknown reasons, on firefox, some #{XXX} template variables are replaced by #%7BXXX%7D :(
function getTemplateContent(templateId){
	var content = document.getElementById(templateId).innerHTML;
	content = content.replace(new RegExp("%7B", "g"), "{");
	content = content.replace(new RegExp("%7D", "g"), "}");
	return content;
}

function ajaxCall(callType, param, successCallback){
	ajaxCall(callType, param, successCallback, false);
}

function ajaxCall(callType, param, successCallback, skipLoading){
	
	if(!skipLoading){
		YAHOO.namespace("global.build.stat.wait");
		YAHOO.global.build.stat.wait.modalPopup =  
	        new YAHOO.widget.Panel("wait",   
	            { width:"240px",
	              fixedcenter:true,  
	              close:false,  
	              draggable:false,  
	              zindex:4, 
	              modal:true
	            }
	        ); 
	        
		YAHOO.global.build.stat.wait.modalPopup.setHeader(document.getElementById('waitMessage').innerHTML);
		YAHOO.global.build.stat.wait.modalPopup.setBody(getTemplateContent('loadingTemplate')); 
	}
			
	YAHOO.global.build.stat.wait.modalPopup.render(document.body);
	if(callType == 'form'){
		const form = document.getElementById(param);
		const formData = new FormData(form);
		const objectFormData = Object.fromEntries(formData.entries());
		fetch(form.action, {
			method: "post",
			headers: crumb.wrap({
				"Content-Type": "application/x-www-form-urlencoded",
			}),
			body: objectToUrlFormEncoded(objectFormData),
		}).then((response) => {
			if(!skipLoading){
				YAHOO.global.build.stat.wait.modalPopup.hide();
			}
			if (response.ok) {
				return response.text()
			} else {
				alert('failure : '+toJsonWorkaround(response));
			}
		}).then((responseText) => {
			successCallback({responseText: responseText});
		});
	} else {
		fetch(param, {
			headers: crumb.wrap({
				"Content-Type": "application/x-www-form-urlencoded",
			}),
		}).then((response) => {
			if(!skipLoading){
				YAHOO.global.build.stat.wait.modalPopup.hide();
			}
			if (response.ok) {
				return response.text()
			} else {
				alert('failure : '+toJsonWorkaround(response));
			}
		}).then((responseText) => {
			successCallback({responseText: responseText});
		});
	}
}	

function deleteBuildStat(buildStatId){
	var deletionConfirmationMessage = document.getElementById('deletionConfirmationMessage').innerHTML;
	if(confirm(deletionConfirmationMessage)){
		ajaxCall('link', 'deleteConfiguration?buildStatId='+buildStatId, function(transport) {
		  	BUILD_STAT_CONFIGS.deleteChart(buildStatId);
		});
	}
}

function toJsonWorkaround(obj){
	// TODO simplify when Prototype.js is removed
	if (Object.toJSON) {
		// Prototype.js
		return Object.toJSON(obj);
	} else {
		// Standard
		return JSON.stringify(obj);
	}
}

function evaluateTemplate(content, context){
	let progressivelyRenderedContent = content
	for (const property in context) {
		progressivelyRenderedContent = progressivelyRenderedContent.replace('#{'+property+'}', context[property]);
	}
	// Removed undefined properties
	progressivelyRenderedContent = progressivelyRenderedContent.replace(/#\{.*?\}/g, '');
	return progressivelyRenderedContent;
}
