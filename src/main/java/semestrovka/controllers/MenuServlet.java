package semestrovka.controllers;

import semestrovka.module.entities.CartModel;
import semestrovka.module.entities.ProductModel;
import semestrovka.module.helpers.Constants;
import semestrovka.module.managers.AbstractFileSystemManager;
import semestrovka.module.managers.FileSystemManager;
import semestrovka.module.managers.ISessionManager;
import semestrovka.module.managers.SessionManager;
import semestrovka.module.repositories.CartRepository;
import semestrovka.module.repositories.CartRepositoryJdbcImpl;
import semestrovka.module.repositories.ProductRepository;
import semestrovka.module.repositories.ProductRepositoryJdbcImpl;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@WebServlet("/menu")
public class MenuServlet extends HttpServlet {

    private ServletContext context;
    private ProductRepository productRepository;
    private CartRepository cartRepository;
    private AbstractFileSystemManager fileSystemManager;
    private ISessionManager sessionManager;

    @Override
    public void init(ServletConfig config) {
        context = config.getServletContext();
        productRepository = (ProductRepositoryJdbcImpl) context.getAttribute(Constants.PRODUCT_REPOSITORY);
        fileSystemManager = (FileSystemManager) context.getAttribute(Constants.FILE_SYSTEM_MANAGER);
        cartRepository = (CartRepositoryJdbcImpl) context.getAttribute(Constants.CART_REPOSITORY);
        sessionManager = (SessionManager) context.getAttribute(Constants.SESSION_MANAGER);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        fileSystemManager.copyFilesToWeb(context.getContextPath());
        req.getSession().setAttribute("products", productRepository.findAll());
        context.getRequestDispatcher("/WEB-INF/views/menu.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        req.setCharacterEncoding("UTF-8");
        if (sessionManager.isAuthenticated(req)) {
            CartModel cartModel = sessionManager.getCart(req);
            String productId = req.getParameter("product-id");
            int id;
            try {
                id = Integer.parseInt(productId);
            } catch (NumberFormatException e) {
                return;
            }
            Optional<ProductModel> productModel = productRepository.findById(id);
            if (productModel.isPresent()) {
                cartModel.addProduct(productModel.get());
                cartRepository.addProduct(sessionManager.getUser(req).getId(), id);
            }
            context.getRequestDispatcher("/WEB-INF/views/menu.jsp").forward(req, resp);
        } else {
            resp.sendRedirect(context.getContextPath()+"/login");
        }
    }
}
