package com.prices.comparator.price_comparator.services;


import com.prices.comparator.price_comparator.dto.ProductResponse;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class PriceComparatorService {


    // Constructor que inicializa el WebDriver
    public PriceComparatorService() {

    }

    private ChromeOptions createChromeOptions() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        return options;
    }


    public ProductResponse searchAllProducts(String query) {


        // Crear CompletableFuture para cada scraping
        CompletableFuture<List<Map<String, String>>> tottusFuture =
                CompletableFuture.supplyAsync(() -> {
                    WebDriver tottusDriver = new ChromeDriver(createChromeOptions());
                    try {
                        return scrapeTottus(query, tottusDriver);
                    }catch(Exception e){
                        return new ArrayList<>();// En caso de error
                    } finally {
                        tottusDriver.quit();
                    }
                });

        CompletableFuture<List<Map<String, String>>> tamboFuture =
                CompletableFuture.supplyAsync(() -> {
                    WebDriver tamboDriver = new ChromeDriver(createChromeOptions());
                    try {
                        return scrapeTambo(query, tamboDriver);
                    } catch (Exception e) {
                        return new ArrayList<>();
                    } finally {
                        tamboDriver.quit();
                    }
                });


        try{
            // Esperar a que ambos completableFuture terminen
            CompletableFuture.allOf(tottusFuture, tamboFuture).join();

            // Obtener resultados
            List<Map<String, String>> tottusResults = tottusFuture.get();
            List<Map<String, String>> tamboResults = tamboFuture.get();

            return new ProductResponse(tottusResults, tamboResults);

        } catch (Exception e) {
            return new ProductResponse(new ArrayList<>(),new ArrayList<>());
            //throw new RuntimeException(e);
        }
    }




    private List<Map<String, String>> scrapeTambo(String searchQuery,WebDriver driver) {
        List<Map<String, String>> products = new ArrayList<>();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        try {
            // Construir la URL y navegar a ella
            String searchUrl = "https://www.tambo.pe/pedir?q=" + searchQuery;
            driver.get(searchUrl);

            Thread.sleep(5000); // Esperar 5 segundos para que la página cargue completamente

            // Esperar a que los elementos de productos estén presentes
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("div._1kZCFobRwQ2tl6XA-sf6Ig.row")));

            // Encontrar todos los elementos de producto
            List<WebElement> productElements = driver.findElements(
                    By.cssSelector("div.col-xs-4:has(span._3tOb1kUYtBNfge7JfPDU_D)"));

            System.out.println("Cantidad de productos encontrados: " + productElements.size());

            for (WebElement productElement : productElements) {
                try {
                    Map<String, String> productData = new HashMap<>();

                    // Extraer nombre del producto
                    WebElement nameElement = productElement.findElement(
                            By.cssSelector("span._3tOb1kUYtBNfge7JfPDU_D"));

                    String productName = nameElement.getText().trim();

                    // Extraer precio
                    WebElement priceElement = productElement.findElement(
                            By.cssSelector("div._3cwJgygKLOPOt2029qoP1N"));
                    String productPrice = priceElement.getText().trim();

                    // Extraer imagen
                    WebElement imageElement = productElement.findElement(By.tagName("img"));
                    String imageUrl = imageElement.getAttribute("src");

                    productData.put("name", productName);
                    productData.put("price", productPrice);
                    productData.put("image", imageUrl);
                    productData.put("source", "tambo");

                    products.add(productData);

                    // Debug
                    System.out.println("Producto encontrado:");
                    System.out.println("Nombre: " + productName);
                    System.out.println("Precio: " + productPrice);
                    System.out.println("Imagen: " + imageUrl);
                    System.out.println("-------------------");

                } catch (Exception e) {
                    System.err.println("Error procesando un producto: " + e.getMessage());
                }
            }

        } catch (Exception e) {
            System.err.println("Error durante el scraping: " + e.getMessage());
            e.printStackTrace();
        }

        return products;
    }


    public List<Map<String, String>> scrapeTottus(String searchQuery,WebDriver driver) {
        List<Map<String, String>> products = new ArrayList<>();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));


        try {

            // Construir la URL y navegar a ella
            String searchUrl = "https://tottus.falabella.com.pe/tottus-pe/search?Ntt=" +  URLEncoder.encode(searchQuery, "UTF-8");
            driver.get(searchUrl);

            Thread.sleep(5000); // Esperar 3 segundos para que la página cargue completamente

            try{
                // Nueva lista de selectores para "sin resultados"
                String[] noResultsSelectors = {
                        "div.jsx-2604302888.no-result",    // Nuevo selector encontrado
                        "div.empty-search",                // Mantener selector anterior por si acaso
                        "h3.jsx-4246891880"               // Selector del mensaje específico
                };

                // Verificar todos los posibles selectores de "sin resultados"
                for (String selector : noResultsSelectors) {
                    try {
                        WebElement noResults = driver.findElement(By.cssSelector(selector));
                        if (noResults != null && noResults.isDisplayed()) {
                            System.out.println("No se encontraron resultados en Tottus para: " + searchQuery);
                            return products; // Retorna lista vacía
                        }
                    } catch (NoSuchElementException e) {
                        continue;
                    }
                }

                // Esperar a que el contenedor principal de productos esté presente
                wait.until(ExpectedConditions.presenceOfElementLocated(
                        By.cssSelector("div.search-results--products")));

                // Encontrar todos los elementos de producto
                // Obtener todos los divs con la clases search-results-4-grid y grid-pod y que debajo tenga
                // un elemento a
                List<WebElement> productElements = driver.findElements(
                        By.cssSelector("div.search-results-4-grid.grid-pod > a"));

                System.out.println("Cantidad de productos encontrados: " + productElements.size());

                for (WebElement productElement : productElements) {
                    try {
                        Map<String, String> productData = new HashMap<>();

                        // Extraer nombre del producto (marca)
                        // Elemento b que tenga las clases pod-title y title-rebrand
                        String brand = productElement.findElement(
                                By.cssSelector("b.pod-title.title-rebrand")
                        ).getText().trim();

                        // Extraer precio
                        // Elemento que tenga la clases copy10 primary y medium
                        String price = productElement.findElement(
                                By.cssSelector("span.copy10.primary.medium")
                        ).getText().trim();

                        // Extraer URL de la imagen
                        // Elemento que tenga el tag img
                        String imageUrl = productElement.findElement(
                                By.cssSelector("img")
                        ).getAttribute("src");


                        productData.put("name", brand);
                        productData.put("price", price);
                        productData.put("image", imageUrl);
                        productData.put("source", "tottus");

                        products.add(productData);

                        // Debug
                        System.out.println("Producto encontrado:");
                        System.out.println("Nombre: " + brand);
                        System.out.println("-------------------");

                    } catch (Exception e) {
                        System.err.println("Error procesando un producto: " + e.getMessage());
                    }
                }
            }catch (Exception e) {
                System.err.println("Error durante el scraping: " + e.getMessage());
                e.printStackTrace();
            }



        } catch (Exception e) {
            System.err.println("Error durante el scraping: " + e.getMessage());
            e.printStackTrace();
        }

        return products;
    }

}