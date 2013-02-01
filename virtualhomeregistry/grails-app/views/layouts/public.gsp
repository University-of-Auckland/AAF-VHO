<!DOCTYPE html>
<html>
  <head>
    <title><g:message code='branding.application.name'/></title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <r:require modules="modernizr, bootstrap, bootstrap-responsive-css, bootstrap-notify, bootstrap-datepicker, bootbox, less, validate, datatables, formrestrict, app"/>
    <r:layoutResources/>
    <g:layoutHead />
  </head>

  <body>
    
    <header>      
      <g:render template='/templates/branding/header' />
    </header>

    <nav>
      <div class="container">
        <div class="navbar">
          <div class="navbar-inner">

            <a class="btn btn-navbar btn-mini" data-toggle="collapse" data-target=".nav-collapse">
              <span class="icon-bar"></span>
              <span class="icon-bar"></span>
            </a>

            <ul class="nav">
              <li>
                <g:link controller="dashboard" action="welcome"><g:message code="branding.nav.welcome" /></g:link>
              </li>
              <li><a href="http://support.aaf.edu.au" target="_blank"><g:message code="branding.nav.support" /></a></li>
            </ul>

          </div>
        </div>
      </div>
    </nav>

    <section>
      <div class="container">
        <div class='notifications top-right'></div>
        <g:layoutBody/>
      </div>
    </section>
  
    <footer>
      <div class="container"> 
        <div class="row">
          <div class="span12">
            <g:render template='/templates/branding/footer' />
          </div>
        </div>
      </div>
    </footer>

    <r:layoutResources/>
  </body>

</html>
