package hudson.plugins.global_build_stats.model;

import hudson.util.DataSetBuilder;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

@ExportedBean
public class BuildStatChartData {
	
	private List<BuildStatChartDimension> dimensions = new ArrayList<BuildStatChartData.BuildStatChartDimension>();
	
	@ExportedBean
	public static class BuildStatChartDimension {
	    private List<Number> values = new ArrayList<Number>();
	    private List<? extends Comparable> rows = new ArrayList<Comparable>();
	    private List<? extends Comparable> columns = new ArrayList<Comparable>();
	    
	    public BuildStatChartDimension(List<Number> values, List<? extends Comparable> rows, List<? extends Comparable> columns){
	    	this.values = values;
	    	this.rows = rows;
	    	this.columns = columns;
	    }
	    
		@Exported
		public List<Number> getValues() {
			return values;
		}

		@Exported
		public List<? extends Comparable> getRows() {
			return rows;
		}

		@Exported
		public List<? extends Comparable> getColumns() {
			return columns;
		}
	}

	public BuildStatChartData(List<AbstractBuildStatChartDimension> dimensions){
		try {
			for(AbstractBuildStatChartDimension dimension : dimensions){
				DataSetBuilder<String, DateRange> dsb = dimension.getDatasetBuilder();
				Field valuesField = dsb.getClass().getDeclaredField("values");
				Field rowsField = dsb.getClass().getDeclaredField("rows");
				Field columnsField = dsb.getClass().getDeclaredField("columns");
				
				valuesField.setAccessible(true);
				rowsField.setAccessible(true);
				columnsField.setAccessible(true);
				
				this.dimensions.add(new BuildStatChartDimension(
						(List<Number>)valuesField.get(dsb), 
						(List<? extends Comparable>)rowsField.get(dsb), 
						(List<? extends Comparable>)columnsField.get(dsb)));
			}
		}catch(NoSuchFieldException e){
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Exported
	public List<BuildStatChartDimension> getDimensions() {
		return dimensions;
	}
}
