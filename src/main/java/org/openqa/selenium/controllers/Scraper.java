package org.openqa.selenium.controllers;

import com.opencsv.*;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static java.lang.Thread.sleep;
import static org.openqa.selenium.Extras.Color.*;


/**
 * La clase Scraper es donde ocurre toda la magia, aqui estan escritos los metodos que se utilizan en este proyecto
 */
public class Scraper {     
    
        public void getCardStats(String name) throws InterruptedException, ExecutionException {

        String nameModified = name.replaceAll(" ", "-");
        String driverPath = "src\\main\\resources\\geckodriver.exe";
        System.setProperty("webdriver.gecko.driver", driverPath);
        WebDriver driver =  new FirefoxDriver();
      
        driver.get("https://www.cardmarket.com/en/Magic/Cards/"+nameModified+"?sellerCountry=10&sellerType=1,2&language=1,4");
        WebDriverWait wait = new WebDriverWait(driver, 10);
        
        loadAllCards(wait, driver);
        //wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("span.d-flex.has-content-centered.me-1")));
    
            // Obtener todos los sellerList con la clase especificada
            
           CompletableFuture<List<String>> sellersFuture = CompletableFuture.supplyAsync(() -> getAllsellers(wait, driver));
        CompletableFuture<List<String>> pricesFuture = CompletableFuture.supplyAsync(() -> getAllPrices(wait, driver));
        CompletableFuture<List<String>> expansionsFuture = CompletableFuture.supplyAsync(() -> getAllexpansions(wait));

        // Espera a que todos los futuros se completen
        CompletableFuture<Void> allOf = CompletableFuture.allOf(sellersFuture, pricesFuture, expansionsFuture);

        // Cuando todos estén completos, obtén los resultados
        allOf.join();  // Espera hasta que todos los futures se completen

        List<String> sellers = sellersFuture.get();
        List<String> prices = pricesFuture.get();
        List<String> expansions = expansionsFuture.get();

           mountOutput(sellers, prices, expansions);
        
        }


        public List<String> getAllsellers(WebDriverWait wait, WebDriver driver){
            List<WebElement> sellerList = driver.findElements(By.cssSelector("span.d-flex.has-content-centered.me-1"));
            List<WebElement> sellerAList;
            List<String> sellerFilterList = new ArrayList<>();
            // Filtrar sellerList que contienen un <a> con href
            for (WebElement span : sellerList) {
                sellerAList = span.findElements(By.tagName("a"));
            for (WebElement seller : sellerAList) {
                String href = seller.getAttribute("href");
                if (href != null && !href.isEmpty()) {
                    sellerFilterList.add(span.getText());
                }
            }
            }
           return sellerFilterList;
        }

        public List<String> getAllPrices(WebDriverWait wait, WebDriver driver){
            List<WebElement> pricesList = driver.findElements(By.cssSelector("span.color-primary.small.text-end.text-nowrap.fw-bold"));
            
            List<String> priceFilterList = new ArrayList<>();
            String filter;
            
            // Obtener todos los precios que sean impar, porque la web internamente pone 2 precios y nos interesa
            //solo 1 ya que queremos que los vendedores, precios, expansiones y estados de la carta sean los mismos
            //para montar los output, el replaceAll con el regex es porque los pilla con un " ?" al final ya que
            //no reconoce el "€"
            for(int i =1; i<pricesList.size();i++){
                if(i%2!=0){
                    filter = pricesList.get(i).getText().replaceAll("[\\s\\?]", "");
                    filter = filter.substring(0, filter.length() - 1);
                    priceFilterList.add(filter);
                }
            }
           return priceFilterList;
        }


        public List<String> getAllexpansions(WebDriverWait wait){
            List<WebElement> expansionList = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector(".expansion-symbol.is-magic.icon.is-24x24.d-flex.me-1")));
             List<String> expansionFilterList = new ArrayList<>();
             //Obtener la expansión de cada carta ofertada
            for (WebElement a : expansionList) {
                expansionFilterList.add(a.getAttribute("data-bs-original-title"));
           }
           return expansionFilterList;
        }

        



























        //este método pulsa unas 4 veces el botón de cargar más para obtener resultados de sobra para comparar
        public void loadAllCards(WebDriverWait wait, WebDriver driver) throws InterruptedException{
            WebElement loadMoreButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("loadMoreButton")));

            // Intenta hacer clic usando JavaScript
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", loadMoreButton);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", loadMoreButton);
            loadMoreButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("loadMoreButton")));
            sleep(1000);

            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", loadMoreButton);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", loadMoreButton);
            loadMoreButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("loadMoreButton")));
            sleep(1000);

            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", loadMoreButton);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", loadMoreButton);
            loadMoreButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("loadMoreButton")));
            sleep(1000);

            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", loadMoreButton);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", loadMoreButton);
            loadMoreButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("loadMoreButton")));
            sleep(1000);
            
        }

        public void mountOutput(List<String> sellerFilterList, List<String> priceFilterList, List<String> expansionFilterList){
            for(int i =0; i<sellerFilterList.size();i++){
                switch (sellerFilterList.get(i)) {
                    case "Levodin":
                        System.out.println("Vendedor: " + PURPLE+sellerFilterList.get(i)+RESET +" Precio: "+priceFilterList.get(i)+" Expansión: "+expansionFilterList.get(i));
                        break;
                    
                    case "Magic-Industria-61":
                        System.out.println("Vendedor: "+ YELLOW+sellerFilterList.get(i)+RESET +" Precio: "+priceFilterList.get(i)+" Expansión: "+expansionFilterList.get(i));
                        break;

                    case "Devian-Magic-Cards":
                        System.out.println("Vendedor: "+ BLUE+sellerFilterList.get(i)+RESET +" Precio: "+priceFilterList.get(i)+" Expansión: "+expansionFilterList.get(i));
                        break;
                    
                    case "inGenio":
                    System.out.println("Vendedor: "+ ORANGE+sellerFilterList.get(i)+RESET +" Precio: "+priceFilterList.get(i)+" Expansión: "+expansionFilterList.get(i));
                        break;

                    case "MagicBarcelona":
                        System.out.println("Vendedor: "+ GREEN+sellerFilterList.get(i)+RESET +" Precio: "+priceFilterList.get(i)+" Expansión: "+expansionFilterList.get(i));
                        break;

                    case "infinitiworld":
                        System.out.println("Vendedor: "+ PINK+sellerFilterList.get(i)+RESET +" Precio: "+priceFilterList.get(i)+" Expansión: "+expansionFilterList.get(i));
                        break;
                    
                    case "PhyrexianMTG":
                        System.out.println("Vendedor: "+ RED+sellerFilterList.get(i)+RESET +" Precio: "+priceFilterList.get(i)+" Expansión: "+expansionFilterList.get(i));
                        break;

                    case "TesoroDragon":
                        System.out.println("Vendedor: "+ CYAN+sellerFilterList.get(i)+RESET +" Precio: "+priceFilterList.get(i)+" Expansión: "+expansionFilterList.get(i));
                        break;

                    default:
                        break;
                }
            }
        }


    }
