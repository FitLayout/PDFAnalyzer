/**
 * PdfAnalyzer.java
 *
 * Created on 12. 1. 2016, 15:04:35 by burgetr
 */
package org.fit.pdf;

import javax.script.ScriptException;

import org.fit.layout.process.ScriptableProcessor;

/**
 * Executes the predefined analysis process.
 * 
 * @author burgetr
 */
public class PdfAnalyzer
{
    private ScriptableProcessor proc;
    private String inputUrl;
    private String outputFile;

    public PdfAnalyzer(String inputUrl, String outputFile)
    {
        this.inputUrl = inputUrl;
        this.outputFile = outputFile;
        proc = new ScriptableProcessor();
    }

    public String getInputUrl()
    {
        return inputUrl;
    }

    public String getOutputFile()
    {
        return outputFile;
    }

    private void runAnalysis()
    {
        try
        {
            proc.put("app", this);
            proc.execInternal("run_analysis.js");
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }
    
    //=============================================================================================
    
    public static void main(String[] args)
    {
        if (args.length == 2)
        {
            PdfAnalyzer pa = new PdfAnalyzer(args[0], args[1]);
            pa.runAnalysis();
            System.exit(0);
        }
        else
            System.err.println("Usage: PdfAnalyzer <input_url> <output_file>");
    }

}
