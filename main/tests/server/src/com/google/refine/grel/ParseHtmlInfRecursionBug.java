package com.google.refine.grel;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.refine.ProjectManager;
import com.google.refine.RefineTest;
import com.google.refine.browsing.Engine;
import com.google.refine.commands.Command;
import com.google.refine.model.Cell;
import com.google.refine.model.ModelException;
import com.google.refine.model.Project;
import com.google.refine.model.Row;




public class ParseHtmlInfRecursionBug extends RefineTest {
    Project project;
    Engine engine;
    ProjectManager ProjectManager;
    protected HttpServletRequest request = null;
    protected HttpServletResponse response = null;
    protected ComputeFacetsCommandRecTest command = null;
    protected StringWriter writer = null;

    public class ComputeFacetsCommandRecTest extends Command {
    	
        /**
         * This command uses POST (probably to allow for larger parameters) but does not actually modify any state
         * so we do not add CSRF protection to it.
         * @throws Exception 
         */
        public void doPost(HttpServletRequest request, HttpServletResponse response, Project project)
                throws Exception {
      
        	
            	Engine engine = getEngine(request, project);
                
                engine.computeFacets();
                
                //here infinite recursion will cause an error...
                respondJSON(response, engine);
            
        }
    }
    
       
    
    @BeforeMethod
    public void SetUp() throws IOException, ModelException  {
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        
        String enginestr = "{\"facets\":[{\"type\":\"list\",\"name\":\"FETCH\",\"columnName\":\"FETCH\",\"expression\":\"grel:value.parseHtml().select(\\n  \\\"meta[property=og:title]\\\"\\n)[0]\",\"omitBlank\":false,\"omitError\":false,\"selection\":[],\"selectBlank\":false,\"selectError\":false,\"invert\":false}],\"mode\":\"row-based\"}\n";
        when(request.getParameter("engine")).thenReturn(enginestr);  
               
        //writer = new StringWriter(); // choose the appropriate writer...
        PrintWriter writy = new PrintWriter(new FileWriter("recursiontest.txt", true));
        try {
        	when(response.getWriter()).thenReturn(writy);     
            //when(response.getWriter()).thenReturn(new PrintWriter(writer));
        } catch (IOException e) {
            e.printStackTrace();
        }      
        
        command = new ComputeFacetsCommandRecTest();
        
        
        
  
        // create the project
        
        project = createProjectWithColumns("RecursionTest", "FETCH");
        Row row = new Row(1);
        Row row2 = new Row(1);
        
        String htmltest = Files.readString(Path.of("tests/data/htmltest.txt"), StandardCharsets.UTF_8);
        row.setCell(0, new Cell(htmltest,null));
        project.rows.add(row);
        
        String htmltest2 = Files.readString(Path.of("tests/data/htmltest2.txt"), StandardCharsets.UTF_8);
        row2.setCell(0, new Cell(htmltest2,null));
        project.rows.add(row2);                
           
    }    
    
    
    @AfterMethod
    public void TearDown() {
        bindings = null;
    }
    
    @Test
    public void testRecursionError() throws Exception {
    	command.doPost(request, response, project) ;
    }
    
}