import POJO.CreateOrder;
import POJO.LoginRequest;
import POJO.LoginResponse;
import POJO.Order;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.specification.RequestSpecification;
import org.testng.Assert;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;

public class ECommerceAPITest {
    public static void main(String[] args) {
        RequestSpecification req=  new RequestSpecBuilder().setBaseUri("https://rahulshettyacademy.com")
                .setContentType(ContentType.JSON).build();
        LoginRequest loginRequest= new LoginRequest();
        loginRequest.setUserEmail("gargi@gmail.com");
        loginRequest.setUserPassword("Gargi@4321");
        RequestSpecification reqLogin=  given().spec(req).body(loginRequest);
        LoginResponse loginResponse= reqLogin.when().post("api/ecom/auth/login")
                .then().extract().response().as(LoginResponse.class);
//        System.out.println(loginResponse.getToken());

//        Add Product
        RequestSpecification addProductReq= new RequestSpecBuilder()
                .setBaseUri("https://rahulshettyacademy.com")
                .addHeader("Authorization",loginResponse.getToken())
                .build();
        RequestSpecification reqAddProduct= given().spec(addProductReq)
                .param("productName","shoe")
                .param("productAddedBy",loginResponse.getMessage())
                .param("productCategory","fashion")
                .param("productSubCategory","shirts")
                .param("productPrice","11500")
                .param("productDescription","Adidas originals")
                .param("productFor","women")
                .multiPart("productImage",new File("C:\\Users\\asus\\Desktop\\doc capg\\profile_pic.jpg"));
        String addProductResponse= reqAddProduct.when().post("api/ecom/product/add-product")
                .then().extract().response().asString();
        JsonPath js= new JsonPath(addProductResponse);
        String productId= js.getString("productId");
        System.out.println("productId "+productId);

//        Create order
        RequestSpecification createOrderspec= new RequestSpecBuilder()
                .setBaseUri("https://rahulshettyacademy.com")
                .setContentType(ContentType.JSON)
                .addHeader("Authorization",loginResponse.getToken())
                .build();
        CreateOrder orders= new CreateOrder();
        Order order= new Order();
        order.setCountry("India");
        order.setProductOrderedId(productId);
        List<Order> li= new ArrayList<Order>();
        li.add(order);
        orders.setOrders(li);

        RequestSpecification createOrder= given().spec(createOrderspec).body(orders);
        String createdOrder= createOrder.when().post("api/ecom/order/create-order")
                            .then().extract().response().asString();
//        System.out.println("Created order "+ createdOrder);

//        Delete Product
        RequestSpecification deleteOrderBaseSpec= new RequestSpecBuilder()
                .setBaseUri("https://rahulshettyacademy.com")
                .addHeader("Authorization",loginResponse.getToken())
                .build();
        RequestSpecification deleteProductSpec= given().spec(deleteOrderBaseSpec).pathParam("productId",productId);
                String deleteResponse= deleteProductSpec.when().delete("api/ecom/product/delete-product/{productId}")
                        .then().extract().response().asString();
                JsonPath jsonPath= new JsonPath(deleteResponse);
        System.out.println(productId);
        Assert.assertEquals("Product Deleted Successfully",jsonPath.getString("message"));
    }
}
