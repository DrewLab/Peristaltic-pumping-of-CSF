/*
 * Peristalsis_demo_2d.java
 */

import com.comsol.model.*;
import com.comsol.model.util.*;
import java.io.File;
import java.util.Scanner;
import java.nio.file.Paths;

/** Model exported on Feb 27 2020, 14:56 by COMSOL 5.4.0.388. */
public class Peristalsis_demo_2d {

// Read parameters	
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
	
    model.label("Peristalsis_demo_2d.mph");
	
	String paramsFile = modelPath + "/PeristalsisDemo2dParams.txt";
	model = setparams(model, paramsFile); // set parameters from the paramFile

    model.component().create("comp1", true);

    

	// create step functions
    model.func().create("step1", "Step");
    model.func().create("step2", "Step");
    model.func("step1").set("location", 0.07);
    model.func("step1").set("smooth", 0.1);
    model.func("step2").set("location", 1.2);
    model.func("step2").set("smooth", 0.4);

 
	
	// create geometries
    model.component("comp1").geom().create("geom1", 2);
    model.component("comp1").geom("geom1").create("r1", "Rectangle");
    model.component("comp1").geom("geom1").feature("r1").set("size", new double[]{12.5, 1});
    model.component("comp1").geom("geom1").run();
	
	// create variables
    model.component("comp1").variable().create("var1");
    String varFile1 = modelPath + "/NS-ALE-2DFormulationVariables.txt";
    model = readVarData(model, varFile1, "var1");
    
    model.component("comp1").variable().create("var2");
    String varFile2 = modelPath + "/PeristalsisDemo2dBC.txt";
    model = readVarData(model, varFile2, "var2");
    model.component("comp1").variable("var1").label("Formulation");
    model.component("comp1").variable("var2").label("Boundaries");
	
	// create weak form PDE's
	// mesh displacement
    model.component("comp1").physics().create("w", "WeakFormPDE", "geom1");
    model.component("comp1").physics("w").field("dimensionless").field("um");
    model.component("comp1").physics("w").field("dimensionless").component(new String[]{"umx", "umy"});
    model.component("comp1").physics("w").create("dir1", "DirichletBoundary", 1);
    model.component("comp1").physics("w").feature("dir1").selection().set(2);
    model.component("comp1").physics("w").create("dir2", "DirichletBoundary", 1);
    model.component("comp1").physics("w").feature("dir2").selection().set(3);
    model.component("comp1").physics("w").active(false);
    model.component("comp1").physics("w").feature("wfeq1").set("weak", new String[][]{{"umWC1"}, {"0"}});
    model.component("comp1").physics("w").feature("dir1").set("r", new String[][]{{"0"}, {"uwall"}});
    model.component("comp1").physics("w").feature("dir2").set("r", new String[][]{{"0"}, {"-uwall"}});
    
    //fluid velocity
    model.component("comp1").physics().create("w2", "WeakFormPDE", "geom1");
    model.component("comp1").physics("w2").field("dimensionless").field("vf");
    model.component("comp1").physics("w2").field("dimensionless").component(new String[]{"vfx", "vfy"});
    model.component("comp1").physics("w2").create("dir1", "DirichletBoundary", 1);
    model.component("comp1").physics("w2").feature("dir1").selection().set(2);
    model.component("comp1").physics("w2").create("dir2", "DirichletBoundary", 1);
    model.component("comp1").physics("w2").feature("dir2").selection().set(3);
    model.component("comp1").physics("w2").feature("wfeq1").set("weak", new String[][]{{"vfWC"}, {"0"}});
    model.component("comp1").physics("w2").feature("dir1").set("r", new String[][]{{"0"}, {"vwall"}});
    model.component("comp1").physics("w2").feature("dir1").label("No slip - bottom wall");
    model.component("comp1").physics("w2").feature("dir2").set("r", new String[][]{{"0"}, {"-vwall"}});
    model.component("comp1").physics("w2").feature("dir2").label("No slip - top wall");
    
    // fluid pressure
    model.component("comp1").physics().create("w3", "WeakFormPDE", "geom1");
    model.component("comp1").physics("w3").field("dimensionless").field("pf");
    model.component("comp1").physics("w3").field("dimensionless").component(new String[]{"pf"});
	model.component("comp1").physics("w3").prop("ShapeProperty").set("order", 1);
    model.component("comp1").physics("w3").feature("wfeq1").set("weak", "pfWC");
	
	// Create mesh
	model.component("comp1").mesh().create("mesh1");
    model.component("comp1").mesh("mesh1").create("map1", "Map");
    model.component("comp1").mesh("mesh1").feature("map1").create("dis1", "Distribution");
    model.component("comp1").mesh("mesh1").feature("map1").create("dis2", "Distribution");
    model.component("comp1").mesh("mesh1").feature("map1").feature("dis1").selection().set(1, 4);
    model.component("comp1").mesh("mesh1").feature("map1").feature("dis2").selection().set(2, 3);
	model.component("comp1").mesh("mesh1").feature("map1").feature("dis1").set("type", "predefined");
    model.component("comp1").mesh("mesh1").feature("map1").feature("dis1").set("elemcount", 7);
    model.component("comp1").mesh("mesh1").feature("map1").feature("dis1").set("elemratio", 2);
    model.component("comp1").mesh("mesh1").feature("map1").feature("dis1").set("symmetric", true);
    model.component("comp1").mesh("mesh1").feature("map1").feature("dis2").set("numelem", 200);
    model.component("comp1").mesh("mesh1").run();

	
    model.component("comp1").view("view1").axis().set("xmin", -1.5040278434753418);
    model.component("comp1").view("view1").axis().set("xmax", 14.0040283203125);
    model.component("comp1").view("view1").axis().set("ymin", -1.9727128744125366);
    model.component("comp1").view("view1").axis().set("ymax", 2.972712993621826);

	//create plots
    //plot 1 - X velocity
    model.result().create("pg1", "PlotGroup2D");
    model.result("pg1").create("surf1", "Surface");
    model.result("pg1").feature("surf1").create("def1", "Deform");
    model.result("pg1").label("Fluid velocity");
    model.result("pg1").feature("surf1").set("expr", "vfx");
    model.result("pg1").feature("surf1").set("descr", "Dependent variable vfx");
    model.result("pg1").feature("surf1").set("smooth", "internal");
    model.result("pg1").feature("surf1").set("resolution", "normal");
    model.result("pg1").feature("surf1").feature("def1")
         .set("expr", new String[]{"uo*umx*Leq/Lo/g1", "uo*umy*Leq/Lo/g1"});
    model.result("pg1").feature("surf1").feature("def1").set("scaleactive", true);
    model.result("pg1").feature("surf1").feature("def1").set("scale",1);
    

	//plot2 - pressure
	model.result().create("pg2", "PlotGroup2D");
	    model.result("pg2").create("surf1", "Surface");
    model.result("pg2").feature("surf1").create("def1", "Deform");
    model.result("pg2").set("edges", false);
    model.result("pg2").label("Fluid Pressure");
    model.result("pg2").feature("surf1").set("expr", "pf");
    model.result("pg2").feature("surf1").set("descr", "Dependent variable pf");
    model.result("pg2").feature("surf1").set("smooth", "internal");
    model.result("pg2").feature("surf1").set("resolution", "normal");
    model.result("pg2").feature("surf1").feature("def1")
         .set("expr", new String[]{"uo*umx*Leq/Lo/g1", "uo*umy*Leq/Lo/g1"});
    model.result("pg2").feature("surf1").feature("def1").set("scaleactive", true);
    model.result("pg2").feature("surf1").feature("def1").set("scale",1);

	// Create study
    model.study().create("std1");
    model.study("std1").create("time", "Transient");
    model.study("std1").feature("time").set("activate", new String[]{"w", "on", "w2", "on", "w3", "on"});

    model.sol().create("sol1");
    model.sol("sol1").study("std1");
    model.sol("sol1").attach("std1");
    model.sol("sol1").create("st1", "StudyStep");
    model.sol("sol1").create("v1", "Variables");
    model.sol("sol1").create("t1", "Time");
    model.sol("sol1").feature("t1").create("fc1", "FullyCoupled");
    model.sol("sol1").feature("t1").feature().remove("fcDef");
	model.study("std1").feature("time").set("tlist", "range(0,0.01,10)");
    model.study("std1").feature("time").set("plot", true);
    model.study("std1").feature("time").set("plotgroup", "pg2");
    model.study("std1").feature("time").set("plotfreq", "tsteps");
    model.study("std1").feature("time")
         .set("discretization", new String[]{"w", "physics", "w2", "physics", "w3", "physics"});
    model.study("std1").feature("time").set("useinitsol", true);

    model.sol("sol1").attach("std1");
    model.sol("sol1").feature("v1").set("resscalemethod", "auto");
    model.sol("sol1").feature("v1").set("clistctrl", new String[]{"t1_t"});
    model.sol("sol1").feature("v1").set("cname", new String[]{"t"});
    model.sol("sol1").feature("v1").set("clist", new String[]{"range(0,0.01,10)"});
    model.sol("sol1").feature("t1").set("control", "time");
    model.sol("sol1").feature("t1").set("tstepsbdf", "intermediate");
    model.sol("sol1").feature("t1").set("initialstepbdfactive", true);
    model.sol("sol1").feature("t1").set("maxstepconstraintbdf", "const");
    model.sol("sol1").feature("t1").set("maxstepbdf", 0.001);
    model.sol("sol1").feature("t1").set("consistent", false);
    model.sol("sol1").feature("t1").set("estrat", "exclude");
    model.sol("sol1").runAll();
    
    // export results for particle tracking
    model.result().export().create("data1", "Data");
    model.result().export("data1").set("expr", new String[]{"umx", "umy", "xcdotx", "xcdoty"});
    model.result().export("data1").set("unit", new String[]{"", "1", "m/s", "m/s"});
    model.result().export("data1").set("descr", new String[]{"", "", "", ""});
    model.result().export("data1")
         .set("filename", modelPath + "/demo2dResults.txt");
    model.result().export("data1").set("location", "grid");
    model.result().export("data1").set("gridx2", "range(0,12.5/200,12.5)");
    model.result().export("data1").set("gridy2", "range(0,1/20,1)");
    model.result().export("data1").set("header", false);
    model.result().export("data1").run();
    
    return model;
  }

  public static void main(String[] args) {
    run();
  }

}
