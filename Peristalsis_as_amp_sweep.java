/*
 * Peristalsis_as_amp_sweep.java
 */

import com.comsol.model.*;
import com.comsol.model.util.*;
import java.io.File;
import java.util.Scanner;
import java.nio.file.Paths;

/** Model exported on Mar 1 2020, 13:47 by COMSOL 5.4.0.388. */
public class Peristalsis_as_amp_sweep {

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
    model.label("Peristalsis_as_amp_sweep.mph");
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
	
	//Outflow surface integration
    model.component("comp1").cpl().create("intop2", "Integration");
    model.component("comp1").cpl("intop2").selection().geom("geom1", 1);
    model.component("comp1").cpl("intop2").selection().set(3);
	model.component("comp1").cpl("intop2").set("opname", "Outflow");
    model.component("comp1").cpl("intop2").set("axisym", true);
    
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
    model.study("std1").create("param", "Parametric");
    model.study("std1").create("time", "Transient");

    model.sol().create("sol1");
    model.sol("sol1").study("std1");
    model.sol("sol1").attach("std1");
    model.sol("sol1").create("st1", "StudyStep");
    model.sol("sol1").create("v1", "Variables");
    model.sol("sol1").create("t1", "Time");
    model.sol("sol1").feature("t1").create("fc1", "FullyCoupled");
    model.sol("sol1").feature("t1").feature().remove("fcDef");
    model.sol().create("sol2");
    model.sol("sol2").study("std1");
    model.sol("sol2").label("Parametric Solutions 1");

    model.batch().create("p1", "Parametric");
    model.batch("p1").create("so1", "Solutionseq");
    model.batch("p1").study("std1");

	
	model.result().dataset().create("rev1", "Revolve2D");
    model.result().dataset().create("rev2", "Revolve2D");
    model.result().dataset().create("tavg1", "TimeAverage");
    model.result().dataset().create("tavg2", "TimeAverage");
    model.result().dataset().create("cpt1", "CutPoint2D");
    model.result().dataset().create("cpt2", "CutPoint2D");
    model.result().dataset("rev2").set("data", "dset2");
    model.result().dataset("tavg1").set("data", "dset2");
    model.result().dataset("tavg2").set("data", "dset2");
    model.result().create("pg1", "PlotGroup2D");
    model.result().create("pg2", "PlotGroup3D");
    model.result().create("pg3", "PlotGroup2D");
    model.result().create("pg5", "PlotGroup2D");
    model.result().create("pg6", "PlotGroup1D");
    model.result().create("pg7", "PlotGroup2D");
    model.result().create("pg8", "PlotGroup1D");
    model.result("pg1").set("data", "dset2");
    model.result("pg1").create("surf1", "Surface");
    model.result("pg1").feature("surf1").create("def1", "Deform");
    model.result("pg2").create("surf1", "Surface");
    model.result("pg2").create("surf2", "Surface");
    model.result("pg2").feature("surf1").create("def1", "Deform");
    model.result("pg2").feature("surf2").create("def1", "Deform");
    model.result("pg3").create("surf1", "Surface");
    model.result("pg5").create("surf1", "Surface");
    model.result("pg6").create("glob1", "Global");
    model.result("pg6").create("glob2", "Global");
    model.result("pg6").feature("glob1").set("data", "dset2");
    model.result("pg6").feature("glob2").set("data", "dset2");
    model.result("pg7").create("surf1", "Surface");
    model.result("pg8").create("ptgr1", "PointGraph");
    model.result("pg8").create("ptgr2", "PointGraph");
    model.result().export().create("data1", "Data");

    model.study("std1").feature("param").set("pname", new String[]{"pow"});
    model.study("std1").feature("param").set("plistarr", new String[]{"2^range(-2,-1,-8)"});
    model.study("std1").feature("param").set("punit", new String[]{""});
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

    model.batch("p1").set("control", "param");
    model.batch("p1").set("pname", new String[]{"pow"});
    model.batch("p1").set("plistarr", new String[]{"2^range(-2,-1,-8)"});
    model.batch("p1").set("punit", new String[]{""});
    model.batch("p1").set("err", true);
    model.batch("p1").feature("so1").set("seq", "sol1");
    model.batch("p1").feature("so1").set("psol", "sol2");
    model.batch("p1").feature("so1")
         .set("param", new String[]{"\"pow\",\"0.25\"", "\"pow\",\"0.125\"", "\"pow\",\"0.0625\"", "\"pow\",\"0.03125\"", "\"pow\",\"0.015625\"", "\"pow\",\"0.0078125\"", "\"pow\",\"0.00390625\""});
    model.batch("p1").attach("std1");
    model.batch("p1").run();

model.result().dataset("rev1").set("startangle", -90);
    model.result().dataset("rev1").set("revangle", 225);
    model.result().dataset("rev2").set("startangle", -90);
    model.result().dataset("rev2").set("revangle", 225);
    model.result().dataset("tavg1").set("looplevelinput", new String[]{"manualindices", "first"});
    model.result().dataset("tavg1").set("looplevelindices", new String[]{"range(17,1,117)", ""});
    model.result().dataset("tavg2").set("looplevelinput", new String[]{"manualindices", "last"});
    model.result().dataset("tavg2").set("looplevelindices", new String[]{"range(17,1,117)", ""});
    model.result().dataset("cpt1").label("Wall point");
    model.result().dataset("cpt1").set("pointx", 1);
    model.result().dataset("cpt1").set("pointy", 5);
    model.result().dataset("cpt2").label("Fluid Point");
    model.result().dataset("cpt2").set("pointx", "Leq*(R2/R1+1)/2");
    model.result().dataset("cpt2").set("pointy", 5);
    model.result().dataset("cpt2").set("pointvar", "cpt1n");
    model.result("pg1").label("Fluid velocity");
    model.result("pg1").set("looplevel", new int[]{117, 1});
    model.result("pg1").feature("surf1").set("expr", "vfz*vo");
    model.result("pg1").feature("surf1").set("unit", "\u00b5m/s");
    model.result("pg1").feature("surf1").set("descr", "vfz*vo");
    model.result("pg1").feature("surf1").set("resolution", "normal");
    model.result("pg1").feature("surf1").feature("def1").set("expr", new String[]{"umr", "umz"});
    model.result("pg1").feature("surf1").feature("def1").set("scale", 0.5);
    model.result("pg1").feature("surf1").feature("def1").set("scaleactive", false);
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
    model.result("pg3").label("Pressure");
    model.result("pg3").set("looplevel", new int[]{5});
    model.result("pg3").feature("surf1").set("expr", "pf");
    model.result("pg3").feature("surf1").set("descr", "Dependent variable pf");
    model.result("pg3").feature("surf1").set("resolution", "normal");
    model.result("pg5").label("Peclet Number High");
    model.result("pg5").set("data", "tavg1");
    model.result("pg5").set("solrepresentation", "solnum");
    model.result("pg5").feature("surf1").set("solrepresentation", "solnum");
    model.result("pg5").feature("surf1").set("expr", "Pe");
    model.result("pg5").feature("surf1").set("resolution", "normal");
    model.result("pg6").label("Reynolds Number");
    model.result("pg6").set("data", "none");
    model.result("pg6").set("xlabel", "Time (s)");
    model.result("pg6").set("twoyaxes", true);
    model.result("pg6")
         .set("plotonsecyaxis", new String[][]{{"Global 1", "off", "glob1"}, {"Global 2", "on", "glob2"}});
    model.result("pg6").set("xlabelactive", false);
    model.result("pg6").feature("glob1").set("looplevelinput", new String[]{"manualindices", "manualindices"});
    model.result("pg6").feature("glob1").set("looplevelindices", new String[]{"range(17,1,67)", "1"});
    model.result("pg6").feature("glob1").set("expr", new String[]{"Re"});
    model.result("pg6").feature("glob1").set("unit", new String[]{"1"});
    model.result("pg6").feature("glob1").set("descr", new String[]{""});
    model.result("pg6").feature("glob2").set("looplevelinput", new String[]{"manualindices", "manual"});
    model.result("pg6").feature("glob2")
         .set("looplevel", new String[]{"1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117", "7"});
    model.result("pg6").feature("glob2").set("looplevelindices", new String[]{"range(17,1,67)", "7"});
    model.result("pg6").feature("glob2").set("expr", new String[]{"Re"});
    model.result("pg6").feature("glob2").set("unit", new String[]{"1"});
    model.result("pg6").feature("glob2").set("descr", new String[]{""});
    model.result("pg7").label("Peclet Number Low");
    model.result("pg7").set("data", "tavg2");
    model.result("pg7").set("solrepresentation", "solnum");
    model.result("pg7").feature("surf1").set("solrepresentation", "solnum");
    model.result("pg7").feature("surf1").set("expr", "Pe");
    model.result("pg7").feature("surf1").set("resolution", "normal");
    model.result("pg8").set("xlabel", "Time (s)");
    model.result("pg8").set("ylabel", "Wall velocity (m/s)");
    model.result("pg8").set("yseclabel", "Fluid velocity (m/s)");
    model.result("pg8").set("twoyaxes", true);
    model.result("pg8")
         .set("plotonsecyaxis", new String[][]{{"Wall velocity", "off", "ptgr1"}, {"Fluid velocity", "on", "ptgr2"}});
    model.result("pg8").set("xlabelactive", false);
    model.result("pg8").set("ylabelactive", false);
    model.result("pg8").set("yseclabelactive", false);
    model.result("pg8").feature("ptgr1").label("Wall velocity");
    model.result("pg8").feature("ptgr1").set("data", "cpt1");
    model.result("pg8").feature("ptgr1").set("looplevelinput", new String[]{"manualindices"});
    model.result("pg8").feature("ptgr1").set("looplevelindices", new String[]{"range(17,1,117)"});
    model.result("pg8").feature("ptgr1").set("expr", "vfr*vo");
    model.result("pg8").feature("ptgr1").set("unit", "m/s");
    model.result("pg8").feature("ptgr1").set("descractive", true);
    model.result("pg8").feature("ptgr1").set("descr", "Wall velocity");
    model.result("pg8").feature("ptgr1").set("legend", true);
    model.result("pg8").feature("ptgr1").set("autopoint", false);
    model.result("pg8").feature("ptgr1").set("autodescr", true);
    model.result("pg8").feature("ptgr2").label("Fluid velocity");
    model.result("pg8").feature("ptgr2").set("data", "cpt2");
    model.result("pg8").feature("ptgr2").set("looplevelinput", new String[]{"manualindices"});
    model.result("pg8").feature("ptgr2").set("looplevelindices", new String[]{"range(17,1,117)"});
    model.result("pg8").feature("ptgr2").set("expr", "vfz*vo");
    model.result("pg8").feature("ptgr2").set("unit", "m/s");
    model.result("pg8").feature("ptgr2").set("descractive", true);
    model.result("pg8").feature("ptgr2").set("descr", "Fluid velocity");
    model.result("pg8").feature("ptgr2").set("legend", true);
    model.result("pg8").feature("ptgr2").set("autopoint", false);
    model.result("pg8").feature("ptgr2").set("autodescr", true);
    model.result().export("data1").set("data", "dset2");
    model.result().export("data1").set("looplevelinput", new String[]{"manualindices", "all"});
    model.result().export("data1").set("looplevelindices", new String[]{"range(17,1,116)", ""});
    model.result().export("data1").set("expr", new String[]{"umr", "xcdotr", "xcdotz"});
    model.result().export("data1").set("unit", new String[]{"1", "m/s", "m/s"});
    model.result().export("data1").set("descr", new String[]{"", "", ""});
    model.result().export("data1").set("filename", modelPath + "/axisymmetricResults.txt");
    model.result().export("data1").set("location", "grid");
    model.result().export("data1").set("gridx2", "range(1.001,(R2/R1*Leq-0.01-1.001)/20,R2/R1*Leq-0.01)");
    model.result().export("data1").set("gridy2", "range(0.01,(g3fac*Leq-0.01-0.01)/100,g3fac*Leq-0.01)");
    model.result().export("data1").set("header", false);
    model.result("pg8").run();
    model.result("pg8").label("Fluid velocity and wall velocity");
    model.result("pg8").run();
    model.result("pg8").run();
    model.result("pg8").feature("ptgr1").set("xdata", "expr");
    model.result("pg8").feature("ptgr1").set("xdataexpr", "t*teq/tau");
    model.result("pg8").feature("ptgr1").set("xdatadescractive", true);
    model.result("pg8").feature("ptgr1").set("xdatadescr", "Time(s)");
    model.result("pg8").feature("ptgr1").set("xdataexpr", "t*tau/teq");
    model.result("pg8").run();
    model.result("pg8").feature("ptgr2").set("xdata", "expr");
    model.result("pg8").feature("ptgr2").set("xdataexpr", "t*tau/teq");
    model.result("pg8").feature("ptgr2").set("xdatadescractive", true);
    model.result("pg8").feature("ptgr2").set("xdatadescr", "Time(s)");
    model.result("pg8").run();
    model.result("pg8").feature("ptgr2").set("xdatadescr", "Time");
    model.result("pg8").run();
    model.result("pg8").feature("ptgr1").set("xdatadescr", "Time");
	model.result().export("data1").run();
	
    return model;
  }

  public static void main(String[] args) {
    run();
  }

}
