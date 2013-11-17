package com.totsp.crossword.net;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Html;

import com.totsp.crossword.FeedParser;
import com.totsp.crossword.io.IO;
import com.totsp.crossword.puz.Box;
import com.totsp.crossword.puz.Puzzle;


/**
 * The Hindu Crossword Puzzles
 * URL: http://www.thehindu.com/
 */
public class THCCDownloader extends AbstractDownloader {
	private static String NAME = "The Hindu Crossword Corner"; 
    DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
    NumberFormat nf = NumberFormat.getInstance();
    
    private static final int NUM_CELLS = 15;
    private static final int GREY_THRESHOLD = 0xf8;

    private Box[][] boxes = new Box[NUM_CELLS][NUM_CELLS];
    private String author = "";
    private String title = "";
    private int numClues = 0;
    ArrayList<String> acrossClues = new ArrayList<String>();
    ArrayList<String> acrossAnswers = new ArrayList<String>();
    ArrayList<String> downClues = new ArrayList<String>();
    ArrayList<String> downAnswers = new ArrayList<String>();
    private boolean inAcross = false;
    private boolean inDown   = false;
    
    public THCCDownloader() {
        super("http://thehinducrosswordcorner.blogspot.com/feeds/posts/default", DOWNLOAD_DIR, NAME);
        nf.setMinimumIntegerDigits(2);
        nf.setMaximumFractionDigits(0);
    }

    public int[] getDownloadDates() {
		return DATE_NO_SUNDAY;
    }

    public String getName() {
        return NAME;
    }

	public String getContent(String url) throws IOException {
		URL u = new URL(url);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		AbstractDownloader.copyStream(u.openStream(), baos);

		return new String(baos.toByteArray());
	}
	
	private File createPuzzle(Date date) {
        File downloadTo = new File(this.downloadDirectory, this.createFileName(date));

        if (downloadTo.exists()) {
            return null;
        }

		try {
	        Puzzle puz = new Puzzle();
	        puz.setAuthor(author);
	        puz.setTitle(title);
	        puz.setDate(date);
	        puz.setWidth(NUM_CELLS);
	        puz.setHeight(NUM_CELLS);
	        puz.setBoxes(boxes);
	        puz.setVersion(IO.VERSION_STRING);
	        puz.setUpdatable(true);
	        puz.setNotes("");
	        puz.setCopyright("Copyright " + date.getYear() + ", The Hindu");
	        
	        //Create a list of rawClues
	        boxes = puz.getBoxes();
        	int rawClueIdx = 0;
        	int acClueIdx = 0;
        	int dnClueIdx = 0;
        	String[] rawClues = new String[numClues];
        	
    		for (int j=0; j<NUM_CELLS; j++) {
            	for (int i=0; i<NUM_CELLS; i++) {
        			if (boxes[j][i] != null) {
	        			if (boxes[j][i].isAcross()) {
	        				int idx = 0;
	        				while ((i+idx<NUM_CELLS) && (boxes[j][i+idx] != null)) {
	        					if (acrossAnswers.get(acClueIdx).length() > idx)
	        						boxes[j][i+idx].setSolution(acrossAnswers.get(acClueIdx).charAt(idx++));
	        					else
	        						break;
	        				}
	        				
	        				rawClues[rawClueIdx++] = this.acrossClues.get(acClueIdx++);
	        			}
	        			if (boxes[j][i].isDown()) {
	        				int idx = 0;
	        				while ((j+idx<NUM_CELLS) && (boxes[j+idx][i] != null)) {
	        					if (downAnswers.get(dnClueIdx).length() > idx)
	        						boxes[j+idx][i].setSolution(downAnswers.get(dnClueIdx).charAt(idx++));
	        					else
	        						break;
	        				}
	        				
	        				rawClues[rawClueIdx++] = this.downClues.get(dnClueIdx++);
	        			}
        			}
        		}
        	}
	        puz.setBoxes(boxes);
        	puz.setRawClues(rawClues);
	        puz.setNumberOfClues(rawClueIdx);

			IO.saveNative(puz, new DataOutputStream(new FileOutputStream(downloadTo)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
            return null;
		} catch (IOException e) {
			e.printStackTrace();
            return null;
		}
		
		return downloadTo;
	}

    public File download(Date date) {
        if (!downloadAndParsePuzzle(date)) {
        	return null;
        }

        return createPuzzle(date);
    }

    private boolean parseGridImage(String imageLink) {
    	if (imageLink.equals("")) {
            LOG.log(Level.SEVERE, "Unable to identify the grid image.");
            return false;
    	} else {
    		URL u;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            try {
				u = new URL(imageLink);
	            AbstractDownloader.copyStream(u.openStream(), baos);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
            Bitmap grid = BitmapFactory.decodeByteArray(baos.toByteArray(), 0, baos.size());
            
            int width = grid.getWidth();
            int height = grid.getHeight();	            
            
            int start_x = 0; int end_x = 0;
            int start_y = 0; int end_y = 0;
            
            boolean first = true;
            
            for (int x=0; x<width; x++) {
            	int c = grid.getPixel(x, height/2) & 0xff;
            	if (c < THCCDownloader.GREY_THRESHOLD) {
            		if (first) { start_x = x; first = false; }
            		end_x = x;
            	}
            }
            first = true;
            for (int y=0; y<height; y++) {
            	int c = grid.getPixel(width/2, y) & 0xff;
            	if (c < THCCDownloader.GREY_THRESHOLD) {
            		if (first) { start_y = y; first = false; }
            		end_y = y;
            	}
            }
            
            float cell_width;
            cell_width = (end_x - start_x) / (float)THCCDownloader.NUM_CELLS;
            cell_width = (cell_width + ((end_y - start_y) / (float)THCCDownloader.NUM_CELLS)) / 2;

            for (float j=start_y+cell_width/2; j<start_y+cell_width*THCCDownloader.NUM_CELLS; j+=cell_width) {
                for (float i=start_x+cell_width/2; i<start_x+cell_width*THCCDownloader.NUM_CELLS; i+=cell_width) {
                    int x = (int) i;
                    int y = (int) j;
                    int diag_x = (int)((end_x + start_x) - i);
                    int diag_y = (int)((end_y + start_y) - j);

                    //Offset centers by 5,5 to the bottom right to avoid the clue numbers in the grid
                    int avg = ((grid.getPixel(x+5, y+5)&0xff)     			+ (grid.getPixel(x+1+5, y+5)&0xff) + 
                    		   (grid.getPixel(x+5, y+1+5)&0xff)   			+ (grid.getPixel(x+1+5, y+1+5)&0xff) +
                    		   (grid.getPixel(diag_x+5, diag_y+5)&0xff)     + (grid.getPixel(diag_x+1+5, diag_y+5)&0xff) + 
                    		   (grid.getPixel(diag_x+5, diag_y+1+5)&0xff)   + (grid.getPixel(diag_x+1+5, diag_y+1+5)&0xff)
                    		  ) / 8;
                    
                    x = (int) ((i-start_x)/cell_width);
                    y = (int) ((j-start_y)/cell_width);
                    if (avg > THCCDownloader.GREY_THRESHOLD) {
                    	boxes[y][x] = new Box();
                    	boxes[y][x].setSolution('X');
                    	boxes[y][x].setResponse(' ');
                    	System.out.print('X');
                    } else {
                    	boxes[y][x] = null;
                    	System.out.print('.');
                    }
                }
                System.out.println();
            }
    	}
    	return true;
    }

    private InputStream getFeed(Date date) {
        	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        	InputStream feedStream = null;
			try {
				URL u = new URL(this.baseUrl + 
									"?updated-min=" + sdf.format(date) + "T00:00:00" +
									"&updated-max=" + sdf.format(date) + "T23:59:59" +
									"&orderby=updated");
				feedStream = u.openStream();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return feedStream;
    }
    
    private boolean parseContent(String html_string) {
    	return parseClues(html_string) && parseGridLink(html_string);
    }
    
    private boolean parseGridLink(String html_string) {
    	String imageLink = null;
	    Document doc = Jsoup.parse(html_string);
	    Elements links = doc.getElementsByTag("a");
	    for (Element link : links) {
	    	if (link.text().compareTo("LINK TO GRID") == 0)
	    	  imageLink = link.attr("href");
	    }
	    
	    return parseGridImage(imageLink);
    }
    
    private void parseClue(String clueLine) {
    	clueLine = android.text.Html.fromHtml(clueLine).toString();
		if (clueLine.contains("ACROSS")) {
			inAcross = true; inDown = false;
		} else if (clueLine.contains("DOWN")) {
			inDown = true; inAcross = false;
		} else if ((inAcross || inDown) && (!clueLine.equals(""))) {
			Matcher clueMatcher = Pattern.compile("\\d+[\\s-]*(.*\\([0-9,\\- ]+\\))[ \\-]*(.+)").matcher(clueLine);
			if (clueMatcher.find()) {
				String clue = clueMatcher.group(1);
				String answer = clueMatcher.group(2);
				answer = answer.replaceAll("[^A-Za-z]", "");
				
				if (inAcross) {
					acrossClues.add(clue);
					acrossAnswers.add(answer);
				} else if (inDown) {
					downClues.add(clue);
					downAnswers.add(answer);
				}
	    		numClues++;
			}
		}
    }

    
    private boolean parseClues(String html_string) {
	    numClues = 0;
	    
	    String text_string = Html.fromHtml(html_string).toString();
	    String lines[] = text_string.split("\\r?\\n");

	    for (int i=0; i<lines.length; i++) {
	    	parseClue(lines[i]);
	    }
	    
		return true;
	}
    
    private boolean downloadAndParsePuzzle(Date date) {
        try {
        	InputStream feedStream = getFeed(date);
        	FeedParser parser = new FeedParser(feedStream);

            this.author = parser.getAuthor();
        	this.title  = parser.getTitle();

        	return parseContent(parser.getContent());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

	@Override
	protected String createUrlSuffix(Date date) {
		return "thcc_" + nf.format(date.getMonth() + 1) + nf.format(date.getDate()) + (date.getYear() + 1900) +
		        ".puz";
	}
}
