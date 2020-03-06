import com.comsol.model.*;
import com.comsol.model.util.*;
import java.io.File;
import java.util.Scanner;
import java.nio.file.Paths;

/** Model exported on Feb 28 2020, 18:53 by COMSOL 5.4.0.388. */
// Modified by Ravi Kedarasetti for readability and modularity
public class Peristalsis_as_gradients {

	public static Model setparams(Model model, String fileName) {
	  File file = new File(fileName);
	  try {
		Scanner sc = new Scanner(file);
	
		while (sc.hasNextLine()){
			String data = sc.nextLine();
			String[] splitData = data.split(" ", 0);
			model.param().set(splitData[0], splitData[1]);
			}
		}
	catch (Exception e) {
		System.out.println("Missing Parameters file :" + fileName);
	}
		return model;
	  
  }
  
  // read variables from files
  public static Model readVarData(Model model, String fileName, String varName) {
	File file = new File(fileName);
	try {
		Scanner sc = new Scanner(file);
		
		while (sc.hasNextLine()){
			String data = sc.nextLine();
			String[] splitData = data.split(" ", 0);
			model.component("comp1").variable(varName).set(splitData[0], splitData[1]);
			}
		}
	catch (Exception e) {
		System.out.println("Missing variable file :" + fileName + e);
	}
		return model;
  }
  
  public static Model run() {
    String modelPath = Paths.get(".").toAbsolutePath().normalize().toString();
    
    Model model = ModelUtil.create("Model");

    model.modelPath(modelPath);
	
	//read parameters
    model.label("Peristalsis_as_gradients.mph");
	String paramsFile = modelPath + "/Peristalsis_as_params.txt";
	model = setparams(model, paramsFile); // set parameters from the paramFile
        
	//create functions
    model.func().create("step1", "Step");
    model.func("step1").set("location", 0.07);

	//create component with axisymmetric geometry
    model.component().create("comp1", true);
	model.component("comp1").geom().create("geom1", 2);
    model.component("comp1").geom("geom1").axisymmetric(true);
    model.component("comp1").geom("geom1").create("r1", "Rectangle");
    model.component("comp1").geom("geom1").feature("r1").set("pos", new String[]{"Leq", "0"});
    model.component("comp1").geom("geom1").feature("r1").set("size", new String[]{"(R2/R1-1)*Leq", "Leq*g3fac"});
    model.component("comp1").geom("geom1").run();
    
    model.component("comp1").mesh().create("mesh1");

    // create variables
    model.component("comp1").variable().create("var1");
    String varFile1 = modelPath + "/NS-ALE-ASFormulationVariables.txt";
    model = readVarData(model, varFile1, "var1");
    
    model.component("comp1").variable().create("var2");
    String varFile2 = modelPath + "/Peristalsis_as_BC.txt";
    model = readVarData(model, varFile2, "var2");
    model.component("comp1").variable("var1").label("Formulation");
    model.component("comp1").variable("var2").label("Boundaries");
    
	//Whole domain integration
    model.component("comp1").cpl().create("intop1", "Integration");
    model.component("comp1").cpl("intop1").selection().all();
    model.component("comp1").cpl("intop1").set("opname", "DInt");
    model.component("comp1").cpl("intop1").set("axisym", true);

    //Mesh displacement
    model.component("comp1").physics().create("w", "WeakFormPDE", "geom1");
    model.component("comp1").physics("w").field("dimensionless").field("um");
    model.component("comp1").physics("w").field("dimensionless").component(new String[]{"umr", "umz"});
    model.component("comp1").physics("w").create("dir1", "DirichletBoundary", 1);
    model.component("comp1").physics("w").feature("dir1").selection().set(4);
    model.component("comp1").physics("w").create("dir2", "DirichletBoundary", 1);
    model.component("comp1").physics("w").feature("dir2").selection().set(1);
    model.component("comp1").physics("w").create("pc1", "PeriodicCondition", 1);
    model.component("comp1").physics("w").feature("pc1").selection().set(2, 3);
    
    model.component("comp1").physics("w").feature("wfeq1").set("weak", new String[][]{{"umWC*2*pi*r/Leq^3"}, {"0"}});
    model.component("comp1").physics("w").feature("dir1").label("Brain tissue");
    model.component("comp1").physics("w").feature("dir2").set("r", new String[][]{{"urad"}, {"0"}});
    model.component("comp1").physics("w").feature("dir2").label("Peristaltic wave");
    model.component("comp1").physics("w").active(false);
    
    // fluid velocity
    model.component("comp1").physics().create("w2", "WeakFormPDE", "geom1");
    model.component("comp1").physics("w2").field("dimensionless").field("vf");
    model.component("comp1").physics("w2").field("dimensionless").component(new String[]{"vfr", "vfz"});
    model.component("comp1").physics("w2").create("dir1", "DirichletBoundary", 1);
    model.component("comp1").physics("w2").feature("dir1").selection().set(4);
    model.component("comp1").physics("w2").create("dir2", "DirichletBoundary", 1);
    model.component("comp1").physics("w2").feature("dir2").selection().set(1);
    model.component("comp1").physics("w2").create("pc1", "PeriodicCondition", 1);
    model.component("comp1").physics("w2").feature("pc1").selection().set(2, 3);
    
    model.component("comp1").physics("w2").feature("wfeq1").set("weak", new String[][]{{"vfWC*2*pi*r/Leq^3"}, {"0"}});
    model.component("comp1").physics("w2").feature("dir1").label("Brain tissue");
    model.component("comp1").physics("w2").feature("dir2").set("r", new String[][]{{"vrad"}, {"0"}});
    model.component("comp1").physics("w2").feature("dir2").label("Peristaltic wave");
    
    //fluid pressure
    model.component("comp1").physics().create("w3", "WeakFormPDE", "geom1");
    model.component("comp1").physics("w3").field("dimensionless").field("pf");
    model.component("comp1").physics("w3").field("dimensionless").component(new String[]{"pf"});
    model.component("comp1").physics("w3").create("pc1", "PeriodicCondition", 1);
    model.component("comp1").physics("w3").feature("pc1").selection().set(2, 3);
    model.component("comp1").physics("w3").create("gconstr1", "GlobalConstraint", -1);
	
	model.component("comp1").physics("w3").prop("ShapeProperty").set("order", 1);
    model.component("comp1").physics("w3").feature("wfeq1").set("weak", "pfWC*2*pi*r");
    model.component("comp1").physics("w3").feature("gconstr1").set("constraintExpression", "DInt(pf)");

    // Mesh
    model.component("comp1").mesh("mesh1").create("map1", "Map");
    model.component("comp1").mesh("mesh1").feature("map1").create("dis1", "Distribution");
    model.component("comp1").mesh("mesh1").feature("map1").create("dis2", "Distribution");
    model.component("comp1").mesh("mesh1").feature("map1").feature("dis1").selection().set(2, 3);
    model.component("comp1").mesh("mesh1").feature("map1").feature("dis2").selection().set(4);
	model.component("comp1").mesh("mesh1").feature("map1").feature("dis1").set("type", "predefined");
    model.component("comp1").mesh("mesh1").feature("map1").feature("dis1").set("elemcount", 9);
    model.component("comp1").mesh("mesh1").feature("map1").feature("dis1").set("elemratio", 2);
    model.component("comp1").mesh("mesh1").feature("map1").feature("dis1").set("symmetric", true);
    model.component("comp1").mesh("mesh1").feature("map1").feature("dis2").set("numelem", 200);
    model.component("comp1").mesh("mesh1").run();
    
    model.view().create("view2", 3);
    model.component("comp1").view("view1").axis().set("xmin", -12.457061767578125);
    model.component("comp1").view("view1").axis().set("xmax", 16.190109252929688);
    model.component("comp1").view("view1").axis().set("ymin", -0.2500004768371582);
    model.component("comp1").view("view1").axis().set("ymax", 10.25);
    model.view("view2").set("showgrid", false);

    

    model.study().create("std1");
    model.study("std1").create("time", "Transient");
    model.sol().create("sol1");
    model.sol("sol1").study("std1");
    model.sol("sol1").attach("std1");
    model.sol("sol1").create("st1", "StudyStep");
    model.sol("sol1").create("v1", "Variables");
    model.sol("sol1").create("t1", "Time");
    model.sol("sol1").feature("t1").create("fc1", "FullyCoupled");
    model.sol("sol1").feature("t1").feature().remove("fcDef");
    
    model.study("std1").feature("time").set("tlist", "range(0,0.25,3.75) range(4,0.02,6)");
    model.study("std1").feature("time").set("useinitsol", true);

    model.sol("sol1").attach("std1");
    model.sol("sol1").feature("v1").set("clist", new String[]{"range(0,0.25,3.75) range(4,0.02,6)", "0.001[s]"});
    model.sol("sol1").feature("t1").set("tlist", "range(0,0.25,3.75) range(4,0.02,6)");
    model.sol("sol1").feature("t1").set("initialstepbdfactive", true);
    model.sol("sol1").feature("t1").set("maxstepconstraintbdf", "const");
    model.sol("sol1").feature("t1").set("maxstepbdf", 0.001);
    model.sol("sol1").feature("t1").set("consistent", false);
    model.sol("sol1").feature("t1").set("estrat", "exclude");
    model.sol("sol1").runAll();

    

    model.result().dataset().create("rev1", "Revolve2D");
    model.result().create("pg2", "PlotGroup3D");
    model.result().create("pg4", "PlotGroup2D");
    model.result().create("pg5", "PlotGroup2D");
    model.result().create("pg6", "PlotGroup2D");
    model.result().create("pg7", "PlotGroup2D");
    model.result("pg2").create("surf1", "Surface");
    model.result("pg2").create("surf2", "Surface");
    model.result("pg2").feature("surf1").create("def1", "Deform");
    model.result("pg2").feature("surf2").create("def1", "Deform");
    model.result("pg4").create("surf1", "Surface");
    model.result("pg4").create("line1", "Line");
    model.result("pg4").feature("surf1").create("def1", "Deform");
    model.result("pg4").feature("line1").create("def1", "Deform");
    model.result("pg5").create("surf1", "Surface");
    model.result("pg5").create("line1", "Line");
    model.result("pg5").feature("surf1").create("def1", "Deform");
    model.result("pg5").feature("line1").create("def1", "Deform");
    model.result("pg6").create("surf1", "Surface");
    model.result("pg6").create("line1", "Line");
    model.result("pg6").feature("surf1").create("def1", "Deform");
    model.result("pg6").feature("line1").create("def1", "Deform");
    model.result("pg7").create("surf1", "Surface");
    model.result("pg7").create("line1", "Line");
    model.result("pg7").feature("surf1").create("def1", "Deform");
    model.result("pg7").feature("line1").create("def1", "Deform");

    
    model.result().dataset("rev1").set("startangle", -90);
    model.result().dataset("rev1").set("revangle", 225);
    model.result("pg2").label("Geometry");
    model.result("pg2").set("looplevel", new int[]{17});
    model.result("pg2").set("titletype", "none");
    model.result("pg2").set("edges", false);
    model.result("pg2").feature("surf1").set("expr", "step1(r/Leq-1)");
    model.result("pg2").feature("surf1").set("unit", "");
    model.result("pg2").feature("surf1").set("descr", "Step 1");
    model.result("pg2").feature("surf1").set("rangecoloractive", true);
    model.result("pg2").feature("surf1").set("rangecolormin", -0.1);
    model.result("pg2").feature("surf1").set("rangecolormax", 1.5);
    model.result("pg2").feature("surf1").set("colortable", "Disco");
    model.result("pg2").feature("surf1").set("colortablerev", true);
    model.result("pg2").feature("surf1").set("resolution", "normal");
    model.result("pg2").feature("surf1").feature("def1").set("revcoordsys", "cylindrical");
    model.result("pg2").feature("surf1").feature("def1").set("expr", new String[]{"umr", "", "umz"});
    model.result("pg2").feature("surf1").feature("def1").set("scale", 0.3);
    model.result("pg2").feature("surf1").feature("def1").set("scaleactive", true);
    model.result("pg2").feature("surf2").set("expr", "step1(r/Leq-R2/R1+0.1)");
    model.result("pg2").feature("surf2").set("unit", "");
    model.result("pg2").feature("surf2").set("descr", "Step 1");
    model.result("pg2").feature("surf2").set("rangecoloractive", true);
    model.result("pg2").feature("surf2").set("rangecolormin", -0.7);
    model.result("pg2").feature("surf2").set("rangecolormax", 1.5);
    model.result("pg2").feature("surf2").set("colortable", "Disco");
    model.result("pg2").feature("surf2").set("resolution", "normal");
    model.result("pg2").feature("surf2").feature("def1").set("revcoordsys", "cylindrical");
    model.result("pg2").feature("surf2").feature("def1").set("expr", new String[]{"umr+0.2", "", "umz"});
    model.result("pg2").feature("surf2").feature("def1").set("scale", 0.3);
    model.result("pg2").feature("surf2").feature("def1").set("scaleactive", true);
    model.result("pg4").label("Radial gradient - computational coordinates");
    model.result("pg4").set("edges", false);
    model.result("pg4").feature("surf1").set("expr", "Leq*vfrr*vo");
    model.result("pg4").feature("surf1").set("unit", "\u00b5m/s");
    model.result("pg4").feature("surf1").set("descr", "Leq*vfrr*vo");
    model.result("pg4").feature("surf1").set("colortable", "WaveLight");
    model.result("pg4").feature("surf1").set("colortablesym", true);
    model.result("pg4").feature("surf1").set("resolution", "normal");
    model.result("pg4").feature("surf1").feature("def1").set("expr", new String[]{"umr", "umz"});
    model.result("pg4").feature("surf1").feature("def1").set("scale", 0.5);
    model.result("pg4").feature("surf1").feature("def1").set("scaleactive", true);
    model.result("pg4").feature("line1").set("coloring", "uniform");
    model.result("pg4").feature("line1").set("color", "black");
    model.result("pg4").feature("line1").set("resolution", "normal");
    model.result("pg4").feature("line1").feature("def1").set("expr", new String[]{"umr", "umz"});
    model.result("pg4").feature("line1").feature("def1").set("scale", 0.5);
    model.result("pg4").feature("line1").feature("def1").set("scaleactive", true);
    model.result("pg5").label("Radial gradient - mesh coordinates");
    model.result("pg5").set("edges", false);
    model.result("pg5").feature("surf1").set("expr", "Leq*vfrr*vo/Lo/g1");
    model.result("pg5").feature("surf1").set("unit", "1/s");
    model.result("pg5").feature("surf1").set("descr", "Leq*vfrr*vo/Lo/g1");
    model.result("pg5").feature("surf1").set("colortable", "WaveLight");
    model.result("pg5").feature("surf1").set("colortablesym", true);
    model.result("pg5").feature("surf1").set("resolution", "normal");
    model.result("pg5").feature("surf1").feature("def1").set("expr", new String[]{"umr", "umz"});
    model.result("pg5").feature("surf1").feature("def1").set("scale", 0.5);
    model.result("pg5").feature("surf1").feature("def1").set("scaleactive", true);
    model.result("pg5").feature("line1").set("coloring", "uniform");
    model.result("pg5").feature("line1").set("color", "black");
    model.result("pg5").feature("line1").set("resolution", "normal");
    model.result("pg5").feature("line1").feature("def1").set("expr", new String[]{"umr", "umz"});
    model.result("pg5").feature("line1").feature("def1").set("scale", 0.5);
    model.result("pg5").feature("line1").feature("def1").set("scaleactive", true);
    model.result("pg6").label("Axial gradient - computational coordinates");
    model.result("pg6").set("edges", false);
    model.result("pg6").feature("surf1").set("expr", "Leq*vfrz*vo");
    model.result("pg6").feature("surf1").set("unit", "\u00b5m/s");
    model.result("pg6").feature("surf1").set("descr", "Leq*vfrz*vo");
    model.result("pg6").feature("surf1").set("colortable", "Twilight");
    model.result("pg6").feature("surf1").set("colortablerev", true);
    model.result("pg6").feature("surf1").set("colortablesym", true);
    model.result("pg6").feature("surf1").set("resolution", "normal");
    model.result("pg6").feature("surf1").feature("def1").set("expr", new String[]{"umr", "umz"});
    model.result("pg6").feature("surf1").feature("def1").set("scale", 0.5);
    model.result("pg6").feature("surf1").feature("def1").set("scaleactive", true);
    model.result("pg6").feature("line1").set("coloring", "uniform");
    model.result("pg6").feature("line1").set("color", "black");
    model.result("pg6").feature("line1").set("resolution", "normal");
    model.result("pg6").feature("line1").feature("def1").set("expr", new String[]{"umr", "umz"});
    model.result("pg6").feature("line1").feature("def1").set("scale", 0.5);
    model.result("pg6").feature("line1").feature("def1").set("scaleactive", true);
    model.result("pg7").label("Axial gradient - mesh coordinates");
    model.result("pg7").set("edges", false);
    model.result("pg7").feature("surf1").set("expr", "Leq*vfrz*vo/Lo/g3");
    model.result("pg7").feature("surf1").set("unit", "1/s");
    model.result("pg7").feature("surf1").set("descr", "Leq*vfrz*vo/Lo/g3");
    model.result("pg7").feature("surf1").set("colortable", "Twilight");
    model.result("pg7").feature("surf1").set("colortablerev", true);
    model.result("pg7").feature("surf1").set("colortablesym", true);
    model.result("pg7").feature("surf1").set("resolution", "normal");
    model.result("pg7").feature("surf1").feature("def1").set("expr", new String[]{"umr", "umz"});
    model.result("pg7").feature("surf1").feature("def1").set("scale", 0.5);
    model.result("pg7").feature("surf1").feature("def1").set("scaleactive", true);
    model.result("pg7").feature("line1").set("coloring", "uniform");
    model.result("pg7").feature("line1").set("color", "black");
    model.result("pg7").feature("line1").set("resolution", "normal");
    model.result("pg7").feature("line1").feature("def1").set("expr", new String[]{"umr", "umz"});
    model.result("pg7").feature("line1").feature("def1").set("scale", 0.5);
    model.result("pg7").feature("line1").feature("def1").set("scaleactive", true);

    return model;
  }

  public static void main(String[] args) {
    run();
  }

}
