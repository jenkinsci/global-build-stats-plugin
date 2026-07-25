/**
 * BuildStatConfigs class definition
 * Needs following things when included :
 * - BUILD_STAT_CONTAINER_ID_PREFIX global constant
 * - chartList.js file inclusion
 */
class BuildStatConfigs {
	constructor(){
		this.ids = new Array();
	}
	add(buildStatConfig){
		this.createChartElement(buildStatConfig);
		this[buildStatConfig.id] = buildStatConfig;
		this.ids[this.size()] = buildStatConfig.id;
	}
	update(bsId, buildStatConfig){
		this.updateChartElement(bsId, buildStatConfig);
		this[buildStatConfig.id] = buildStatConfig;
		
		// Is id is updated, remove old id reference
		if(bsId != buildStatConfig.id){
			this[bsId] = null;
			this.ids.splice(this.ids.indexOf(bsId), 1);
		}
	}
	// renamed from delete to deleteChart since in chrome_linux, delete is a reserved keyword
	deleteChart(buildStatId){
		this.deleteChartElement(buildStatId);
		this[buildStatId] = null;
		this.ids.splice(this.ids.indexOf(buildStatId), 1);
	}
	deleteChartElement(buildStatId){
		var buildStatContainerId = BUILD_STAT_CONTAINER_ID_PREFIX+buildStatId;
		var previousBuildStatContainer = this.getPreviousBuildStatConfigContainer(buildStatContainerId);
		document.getElementById(buildStatContainerId).innerHTML = "";
		document.getElementById(buildStatContainerId).id="deletedBuildStatConfig";
		if(previousBuildStatContainer != null){
			this.updateButtonsFor(this.retrieveBuildStatIdFromContainerId(previousBuildStatContainer.id));
		}
	}
	getBuildStat(buildStatId){
		return this[buildStatId];
	}
	size(){
		return this.ids.length;
	}
	getHTMLWithoutContainerFromBuildStatConfig(buildStatConfiguration){
		var currentContext = createTemplateContext(buildStatConfiguration);
		
		var imageTemplateStr = '';
		imageTemplateStr += '<img style="display:inline; float:left; margin-bottom: 10px; margin-right: 10px;" id="img_#{id}" \n';
		imageTemplateStr += 'src="#{rootURL}/plugin/global-build-stats/showChart?buildStatId=#{id}&time=#{currentTime}" />\n';
		imageTemplateStr += '<div id="map_#{id}_container"></div><br/>\n';
		var image = evaluateTemplate(imageTemplateStr, currentContext);
		
		currentContext = jsonConcat(currentContext, { buildStatImage: image,
			unsanitized: ['buildStatImage']});
		
		var buildStatConfigWithoutContainerTemplate = getTemplateContent('buildStatConfigWithoutContainerTemplate');
		var buildStatConfigWithoutContainerHTML = evaluateTemplate(buildStatConfigWithoutContainerTemplate, currentContext);
		
		return buildStatConfigWithoutContainerHTML;
	}
	isBuildStatConfigContainer(htmlElement){
		return htmlElement != null && htmlElement.id != null && htmlElement.id.startsWith(BUILD_STAT_CONTAINER_ID_PREFIX);
	}
	getPreviousBuildStatConfigContainer(currentBuildStatConfigContainerId){
		var container = document.getElementById(currentBuildStatConfigContainerId).previousElementSibling;
		while(container != null && !this.isBuildStatConfigContainer(container)){
			container = container.previousElementSibling;
		}
		return container;
	}
	getNextBuildStatConfigContainer(currentBuildStatConfigContainerId){
		var container = document.getElementById(currentBuildStatConfigContainerId).nextElementSibling;
		while(container != null && !this.isBuildStatConfigContainer(container)){
			container = container.nextElementSibling;
		}
		return container;
	}
	updateButtonsFor(buildStatConfigId){
		var containerId = BUILD_STAT_CONTAINER_ID_PREFIX+buildStatConfigId;
		var container = document.getElementById(containerId);
		
		if(document.getElementById('moveUp_'+buildStatConfigId) != null){
			if(this.getPreviousBuildStatConfigContainer(containerId) != null){
				document.getElementById('moveUp_'+buildStatConfigId).style.display = '';
			} else {
				document.getElementById('moveUp_'+buildStatConfigId).style.display = 'none';
			}
		}
		if(document.getElementById('moveDown_'+buildStatConfigId) != null){
			if(this.getNextBuildStatConfigContainer(containerId) != null){
				document.getElementById('moveDown_'+buildStatConfigId).style.display = '';
			} else {
				document.getElementById('moveDown_'+buildStatConfigId).style.display = 'none';
			}
		}
    Behaviour.applySubtree(container);
	}
	swapBuildStatConfigs(containerId1, containerId2){
		var buildStatConf1 = this.getBuildStatConfigFromContainerId(containerId1);
		var buildStatConf2 = this.getBuildStatConfigFromContainerId(containerId2);
	
		var buildStatConf1Container = document.getElementById(BUILD_STAT_CONTAINER_ID_PREFIX+buildStatConf1.id);
		var buildStatConf2Container = document.getElementById(BUILD_STAT_CONTAINER_ID_PREFIX+buildStatConf2.id);
		
		var replacedHTML = buildStatConf1Container.innerHTML;
		buildStatConf1Container.innerHTML = buildStatConf2Container.innerHTML;
		buildStatConf2Container.innerHTML = replacedHTML;
		
		buildStatConf1Container.id = BUILD_STAT_CONTAINER_ID_PREFIX+buildStatConf2.id;
		buildStatConf2Container.id = BUILD_STAT_CONTAINER_ID_PREFIX+buildStatConf1.id;
		
		this.updateButtonsFor(buildStatConf1.id);
		this.updateButtonsFor(buildStatConf2.id);
	}
	retrieveBuildStatIdFromContainerId(containerId){
		var extractingRegex = new RegExp("^"+BUILD_STAT_CONTAINER_ID_PREFIX+"(.*)$", "g");
		var buildStatId = null;
		if(extractingRegex.test(containerId)){
			extractingRegex.exec(containerId);
			buildStatId = RegExp.$1;
		}
		return buildStatId;
	}
	getBuildStatConfigFromContainerId(containerId){
		var buildStatConfigId = this.retrieveBuildStatIdFromContainerId(containerId);
		var buildStatConfig = null;
		if(buildStatConfigId != null){
			buildStatConfig = this.getBuildStat(buildStatConfigId);
		}
		return buildStatConfig;
	}
	fillDivWithChart(divId, buildStatConfig, updateButtonsCallback){
		ajaxCall('link', rootURL+'/plugin/global-build-stats/createChartMap?buildStatId='+buildStatConfig.id, function(ret){
			var content = BUILD_STAT_CONFIGS.getHTMLWithoutContainerFromBuildStatConfig(buildStatConfig);
			document.getElementById(divId).innerHTML = content;
			document.getElementById('map_'+buildStatConfig.id+'_container').innerHTML = ret.responseText;
			var mapId = "map_"+buildStatConfig.id;
			document.getElementById('map_'+buildStatConfig.id+'_container').firstChild.setAttribute("name", mapId);
			document.getElementById('img_'+buildStatConfig.id).setAttribute("usemap", "#" + mapId);
			
			updateButtonsCallback.call(null);
		}, true);
	}
	updateChartElement(bsId, buildStatConfig){
		document.getElementById(BUILD_STAT_CONTAINER_ID_PREFIX+bsId).id = BUILD_STAT_CONTAINER_ID_PREFIX+buildStatConfig.id;
		this.fillDivWithChart(BUILD_STAT_CONTAINER_ID_PREFIX+buildStatConfig.id, buildStatConfig, function(){
			BUILD_STAT_CONFIGS.updateButtonsFor(buildStatConfig.id);
		});
	}
	createChartElement(buildStatConfig){
		if(this.size() == 0){
			document.getElementById('buildStatConfigsContainer').innerHTML = "";
		}
		
		var newBuildStatContainerId = BUILD_STAT_CONTAINER_ID_PREFIX+buildStatConfig.id;
		
		// This stuff could be simpler with Prototype 1.6...
		var newBuildStatConf = document.createElement("div");
		newBuildStatConf.setAttribute("style", "clear:left");
		newBuildStatConf.setAttribute("id", newBuildStatContainerId);

		document.getElementById('buildStatConfigsContainer').appendChild(newBuildStatConf);
		this.fillDivWithChart(newBuildStatContainerId, buildStatConfig, function(){
			BUILD_STAT_CONFIGS.updateButtonsFor(buildStatConfig.id);
			var previousBuildStatContainer = BUILD_STAT_CONFIGS.getPreviousBuildStatConfigContainer(newBuildStatContainerId);
			if(previousBuildStatContainer != null){
				BUILD_STAT_CONFIGS.updateButtonsFor(BUILD_STAT_CONFIGS.retrieveBuildStatIdFromContainerId(previousBuildStatContainer.id));
			}
    });
  }
	moveBuildStat(buildStatId, moveType){
		var moveUrl = "";
		if(moveType.toLowerCase() == "up"){
			moveUrl = rootURL+'/plugin/global-build-stats/moveUpConf?buildStatId='+buildStatId;
		} else if(moveType.toLowerCase() == "down"){
			moveUrl = rootURL+'/plugin/global-build-stats/moveDownConf?buildStatId='+buildStatId;
		}
		
		ajaxCall('link', moveUrl, function(transport) {
		  	var currentContainerId = BUILD_STAT_CONTAINER_ID_PREFIX+buildStatId;
			var currentChartContainer = document.getElementById(currentContainerId);
			var otherChartContainer = null;
			if(moveType.toLowerCase() == "up"){
				otherChartContainer = BUILD_STAT_CONFIGS.getPreviousBuildStatConfigContainer(currentContainerId);
			} else if(moveType.toLowerCase() == "down"){
				otherChartContainer = BUILD_STAT_CONFIGS.getNextBuildStatConfigContainer(currentContainerId);
			}
			
			BUILD_STAT_CONFIGS.swapBuildStatConfigs(currentChartContainer.id, otherChartContainer.id);
			
			window.location.href = "#"+BUILD_STAT_CONTAINER_ID_PREFIX+buildStatId;
		});
	}
};
