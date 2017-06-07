<%@ page import="java.util.*" %>
<%@ page import="java.io.*" %>
<%@ page import="java.net.*" %>
<%@ page import="itm.image.*" %>
<%@ page import="itm.model.*" %>
<%@ page import="itm.util.*" %>
<!--
/*******************************************************************************
 This file is part of the WM.II.ITM course 2016
 (c) University of Vienna 2009-2016
 *******************************************************************************/
-->
<%
       
%>
<html>
    <head>
    </head>
    <body>

        
        fill in your code here :)
        <%
        
            String tag = null;

            // ***************************************************************
            //  Fill in your code here!
            // ***************************************************************

            // get "tag" parameter   
            
            // if no param was passed, forward to index.jsp (using jsp:forward)

        %>

        <h1>Media that is tagged with <%= tag %></h1>
        <a href="index.jsp">back</a>

        <%

            // ***************************************************************
            //  Fill in your code here!
            // ***************************************************************
        
            // get all media objects that are tagged with the passed tag
            
            // iterate over all available media objects and display them
                
        %>
        
    </body>
</html>
