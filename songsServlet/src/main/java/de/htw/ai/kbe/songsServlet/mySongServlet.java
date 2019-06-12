package de.htw.ai.kbe.songsServlet;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;



public class mySongServlet extends HttpServlet{

	private static final String PERSISTENCE_UNIT_NAME = "songDB";
	private static final EntityManagerFactory ENTITY_MANAGER_FACTORY = Persistence.createEntityManagerFactory("songDB");
	
	
	private static final long serialVersionUID = 1L;
	
	List<OurSong> readSongs;	
	
	private String uriToDB = null;
	private String xmlFilePath = null;

	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
//	    // Beispiel: Laden eines Konfigurationsparameters aus der web.xml
	this.uriToDB = servletConfig.getInitParameter("uriToDBComponent");
//		this.xmlFilePath = servletConfig.getInitParameter("xmlPath");
//		
//    	try 
//    	{
//    		readSongs = readXMLToSongs(xmlFilePath);
//        	
//        } 
//    	catch(FileNotFoundException e)
//    	{
//    		readSongs = null;
//    		System.out.println(e);
//    	}
//    	catch (Exception e) 
//    	{
//    		System.out.println(e);
//    	}
//    	
//    	for (OurSong song : readSongs) {
//    		addSong(song.getId(), song.getTitle(), song.getArtist(), song.getAlbum(), song.getReleased());
//       	}
	}
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		// alle Parameter (keys)
		
		
		ObjectMapper mapper = new ObjectMapper();
		OurSong responseObj = null;
		String jsonString = "";
		
		
		Enumeration<String> paramNames = request.getParameterNames();
		List<String> param = new ArrayList<String>();
		while (paramNames.hasMoreElements()) {
			param.add(paramNames.nextElement());
		}
		
		Enumeration<String> headerNames = request.getHeaderNames();
		List<String> header = new ArrayList<>();
		while (headerNames.hasMoreElements()) {
			header.add(headerNames.nextElement());
		}
		
		if(header.contains("accept"))
		{
			if(param.isEmpty())
			{
				try(PrintWriter out = response.getWriter()) 
				{
					
				//	String a = "";
					response.setContentType("text/html");
			        PrintWriter outs = response.getWriter();
			        outs.println(readSongs.toString());
					
					for(OurSong song : readSongs)
					{
				    	jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(song);
				    	out.println(jsonString);
					}
				}
				catch(Exception e)
				{
					response.setContentType("text/html");
			        PrintWriter outs = response.getWriter();
			        outs.println("exception");
				}
			}
			else if(param.contains("songId"))
			{
				if(!(request.getParameter("songId").isEmpty()))
				{
					try(PrintWriter out = response.getWriter()) 
					{
						int id = Integer.parseInt(request.getParameter("songId"));
						
						for (OurSong song : readSongs) {
							if (id == song.getId()) {
								responseObj = readSongs.get(id);
							}
						}
						
				    	
				    	jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(responseObj);
				    	out.println(jsonString);
					}
					catch(ClassCastException e)
					{
						//falscher value
					}
					catch(IndexOutOfBoundsException e)
					{
						//kein Song mit der nummer vorhanden
					}
					catch(Exception e)
					{
						System.out.println(e.getMessage());
					}
				}
				else
				{
					String a = "";
					response.setContentType("text/html");
			        PrintWriter outs = response.getWriter();
			        outs.println("readSongs list");
				}
			}
			else
			{
				//falsche/unbekannte parameter angabe
				response.setStatus(400);
				PrintWriter pr = response.getWriter();
				pr.print(response.getStatus());
			}
		}
		else
		{
			//nicht unterstützter Accept-Header-Wert
			response.setStatus(406);
			PrintWriter pr = response.getWriter();
			pr.print(response.getStatus());
		}
		
		
		
		/*response.setContentType("text/plain");
		try (PrintWriter out = response.getWriter()) {
			out.println(jsonString);
		}*/
	}
	
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setContentType("text/plain");
		ServletInputStream inputStream = request.getInputStream();
		byte[] inBytes = IOUtils.toByteArray(inputStream);
		try (PrintWriter out = response.getWriter()) {
			out.println(new String(inBytes));
		}
	}
	
	 public static void addSong(int id, String title, String artist, String album, int released) {
	        // The EntityManager class allows operations such as create, read, update, delete
	        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();
	        // Used to issue transactions on the EntityManager
	        EntityTransaction et = null;
	 
	        try {
	            // Get transaction and start
	            et = em.getTransaction();
	            et.begin();
	 
	            // Create and set values for new customer
	            OurSong song = new OurSong();
	            song.setId(id);
	            song.setTitle(title);
	            song.setAlbum(album);
	            song.setArtist(artist);
	            song.setReleased(released);
	 
	            // Save the customer object
	            em.persist(song);
	            et.commit();
	        } catch (Exception ex) {
	            // If there is an exception rollback changes
	            if (et != null) {
	                et.rollback();
	            }
	            ex.printStackTrace();
	        } finally {
	            // Close EntityManager
	            em.close();
	        }
	    }
	
	protected String getUriToDB () {
		return this.uriToDB;
	}
	
	private List<OurSong> unmarshal(Unmarshaller unmarshaller, Class<OurSong> clazz, String xmlLocation)throws JAXBException 
	{
	    StreamSource xml = new StreamSource(xmlLocation);
	    SongsWrapper wrapper = (SongsWrapper) unmarshaller.unmarshal(xml, SongsWrapper.class).getValue();
	    return wrapper.getSongs();
	}
	
	private List<OurSong> readXMLToSongs(String filename) throws JAXBException, FileNotFoundException, IOException 
	{
	    JAXBContext context = JAXBContext.newInstance(SongsWrapper.class, OurSong.class);
	    Unmarshaller unmarshaller = context.createUnmarshaller();
	    try (InputStream is = new BufferedInputStream(new FileInputStream(filename))) {
	        List<OurSong> songs = unmarshal(unmarshaller, OurSong.class, filename);
	        return songs;
	    }
	}
	
	private void readJSONToSongs(String jsonObj) 
	{
        ObjectMapper objectMapper = new ObjectMapper();
        try {
			OurSong o = objectMapper.readValue(jsonObj, OurSong.class);
			
			int i = readSongs.size() +1;
			boolean b = true;
			while(b)
			{
				for(OurSong os : readSongs)
				{
					if(os.getId() == i)
					{
						b = false;
					}
				}
				if(!b) 
				{
					i++;
					b = true;
				}
				else
				{
					o.setId(i);
				}
				
			}
			
			readSongs.add(o);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
	
	
}
