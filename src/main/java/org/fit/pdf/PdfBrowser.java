/**
 * BlockBrowserTest.java
 *
 * Created on 24. 2. 2015, 9:21:16 by burgetr
 */

package org.fit.pdf;

import java.awt.EventQueue;
import java.net.URL;

import javax.swing.JFrame;

import org.fit.layout.tools.BlockBrowser;

/**
 * 
 * @author burgetr
 */
public class PdfBrowser extends BlockBrowser
{

    public static void main(String[] args)
    {
        EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                try
                {
                    browser = new BlockBrowser();
                    browser.setLoadImages(false);
                    JFrame main = browser.getMainWindow();
                    //main.setSize(1000,600);
                    //main.setMinimumSize(new Dimension(1200, 600));
                    //main.setSize(1500,600);
                    main.setSize(1600,1000);
                    browser.initPlugins();
                    main.setVisible(true);
                    
                    String localpath = "file://" + System.getProperty("user.home");
                    localpath += "/git/TestingLayout";
            
                    //URL url = new URL("http://www.idnes.cz/");
                    //URL url = new URL("http://olomouc.idnes.cz/rad-nemeckych-rytiru-pozadal-v-restitucich-i-o-hrady-bouzov-a-sovinec-12b-/olomouc-zpravy.aspx?c=A131113_115042_olomouc-zpravy_mip");
                    //URL url = new URL("http://www.aktualne.cz/");
                    
                    /* SCHEDULES */
                    
                    URL url = new URL(localpath + "/test/schedule/brno30.pdf");
                    //URL url = new URL(localpath + "/test/schedule/dpmb_z_30.pdf");
                    //URL url = new URL(localpath + "/test/schedule/dpmb_30_kamenolom.pdf");
                    
                    /* PROGRAMMES */
                    
                    //URL url = new URL("http://faculty.neu.edu.cn/yangxc/DQIS2011/workshop.html");
                    //URL url = new URL("http://dali2011.dia.uniroma3.it/program.html");
                    //URL url = new URL("http://www.searchingspeech.org/SSCS2010/SSCS2010.html");
                    //URL url = new URL("http://sspnet.eu/2010/04/sspw/");
                    //URL url = new URL("http://www.icudl2010.org/icudl_program.htm"); //problemy - prazdne vyrazne oblasti?
                    //URL url = new URL("http://iwssps2010.cs.arizona.edu/program.html");
                    //URL url = new URL("http://www.cssim.org/sites/cssim.org/files/cssim-timetable-full.html");
                    //URL url = new URL("http://liber2009.biu-toulouse.fr/images/stories/documents/conference_programme_toulouse_EN.pdf");
                    //URL url = new URL("http://www.ehealthconference.info/conferenceprogramme/index.php");
                    //URL url = new URL("http://www.dexa.org/previous/dexa2011/programme703b.html?cid=189");
                    //URL url = new URL("http://www.icdar2011.org/EN/column/column32.shtml");
                    //URL url = new URL("http://aktualne.centrum.cz/ekonomika/business-ve-svete/clanek.phtml?id=749291");
                    //URL url = new URL("http://clair.si.umich.edu/clair/sigmod-pods06/SIGMOD-program.htm");
                    //URL url = new URL("http://edbticdt2011.it.uu.se/workshops_program.html");
                    //URL url = new URL("http://www.znalosti.eu/program-konference");
                    //URL url = new URL(localpath + "/test/simple.html");
                    //URL url = new URL(localpath + "/test/markedness.html");
                    //URL url = new URL(localpath + "/test/programmes/dqis2011.html");
                    //URL url = new URL(localpath + "/test/programmes/SSCS2010.html");
                    //URL url = new URL(localpath + "/test/programmes/icudl2010.html");
                    //URL url = new URL(localpath + "/test/programmes/iwssps2010.html");
                    //URL url = new URL(localpath + "/test/programmes/dali2011.html");
                    //URL url = new URL(localpath + "/test/programmes/RuleML-2010-Programme.pdf");
                    //URL url = new URL(localpath + "/test/programmes/cade23-schedule.pdf");
                    //URL url = new URL(localpath + "/test/programmes/ehealth07.html");
                    //URL url = new URL(localpath + "/test/programmes/znalosti2013.html");
                    //URL url = new URL(localpath + "/test/programmes2/1/aaa-idea.org/program.shtml");            
                    //URL url = new URL(localpath + "/test/programmes2/2/aciids2010.hueuni.edu.vn/index.html");
                    //URL url = new URL(localpath + "/test/programmes3/x37/index.html");

                    
                    /* MENUS */

                    //URL url = new URL("http://menu.olomouc.cz/index.php?act=rmenu&rid=32");
                    //URL url = new URL("http://www.obedvat.cz/cz/obedove-menu/619-pivni-bar-atrium.html");
                    
                    /* NEWS */
                    //URL url = new URL("http://edition.cnn.com/2014/02/24/world/europe/ukraine-protests-up-to-speed/index.html?hpt=hp_t1");
                    //URL url = new URL("http://www.reuters.com/article/2014/03/28/us-trading-momentum-analysis-idUSBREA2R09M20140328");
                    
                    /* CEUR */
                    //URL url = new URL(localpath + "/test/ceur/volumes/Vol-1317.html");
                    
                    browser.setLocation(url.toString());
                        
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

}
