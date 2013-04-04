package com.nuodb.storefront.service.simulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.googlecode.genericdao.search.SearchResult;
import com.nuodb.storefront.exception.UnsupportedStepException;
import com.nuodb.storefront.model.Cart;
import com.nuodb.storefront.model.CartSelection;
import com.nuodb.storefront.model.Category;
import com.nuodb.storefront.model.Customer;
import com.nuodb.storefront.model.Product;
import com.nuodb.storefront.model.ProductFilter;
import com.nuodb.storefront.model.ProductSort;
import com.nuodb.storefront.model.WorkloadStep;
import com.nuodb.storefront.model.Workload;

/**
 * Runs through the steps specified by a {@class WorkloadType} field.
 */
public class SimulatedUser implements IWorker {
    private final ISimulator simulator;
    private final Workload workloadType;
    private final Random rnd = new Random();
    private int stepIdx;

    private Customer customer;
    private ProductFilter filter;
    private SearchResult<Product> products;
    private SearchResult<Category> categories;
    private Product product;
    private Cart cart;

    public SimulatedUser(ISimulator simulator, Workload workloadType) {
        if (workloadType == null) {
            throw new IllegalArgumentException("workloadType");
        }
        this.simulator = simulator;
        this.workloadType = workloadType;
    }

    @Override
    public Workload getWorkload() {
        return workloadType;
    }

    @Override
    public long doWork() {
        WorkloadStep[] steps = workloadType.getSteps();

        if (steps.length == 0) {
            return IWorker.COMPLETE_NO_REPEAT;
        }

        doWork(steps[stepIdx]);
        stepIdx++;

        if (stepIdx >= steps.length) {
            stepIdx = 0;
            return IWorker.COMPLETE;
        }

        return workloadType.calcNextThinkTimeMs();
    }

    protected void doWork(WorkloadStep step) {
        customer = simulator.getService().getOrCreateCustomer((customer == null) ? 0 : customer.getId());

        switch (step) {
            case BROWSE:
                doBrowse();
                break;

            case BROWSE_NEXT_PAGE:
                doBrowseNextPage();
                break;

            case BROWSE_SEARCH:
                doBrowseSearch();
                break;

            case BROWSE_CATEGORY:
                doBrowseCategory();
                break;

            case BROWSE_SORT:
                doBrowseCategory();
                break;

            case PRODUCT_VIEW_DETAILS:
                doProdutViewDetails();
                break;

            case PRODUCT_ADD_TO_CART:
                doProductAddToCart();
                break;

            case PRODUCT_ADD_REVIEW:
                doProductAddReview();
                break;

            case CART_VIEW:
                doCartView();
                break;

            case CART_UPDATE:
                doCartUpdate();
                break;

            case CART_CHECKOUT:
                doCartCheckout();
                break;
                
            case ADMIN_RUN_REPORT:
                doRunReport();
                break;

            default:
                throw new UnsupportedStepException();
        }

        simulator.incrementStepCompletionCount(step);
    }

    protected void doBrowse() {
        filter = new ProductFilter();
        products = simulator.getService().getProducts(filter);
        categories = simulator.getService().getCategories();
    }

    protected void doBrowseNextPage() {
        getOrFetchProductList();
        filter.setPage(filter.getPage() + 1);
        products = simulator.getService().getProducts(filter);
    }

    protected void doBrowseSearch() {
        filter = new ProductFilter();
        filter.setCategories(new ArrayList<String>());
        if (getOrFetchProductList()) {
            Product product = products.getResult().get(rnd.nextInt(products.getResult().size()));
            filter.setMatchText(product.getName().substring(0, Math.min(5, product.getName().length())));
        } else {
            filter.setMatchText("DNE search");
        }
    }

    protected void doBrowseCategory() {
        filter = new ProductFilter();
        filter.setCategories(new ArrayList<String>());
        if (getOrFetchCategories()) {
            Category category = pickRandomCategory();
            filter.getCategories().add(category.getName());
        } else {
            filter.getCategories().add("DNE category");
        }
        products = simulator.getService().getProducts(filter);
        categories = simulator.getService().getCategories();
    }

    protected void doBrowseSort() {
        filter = new ProductFilter();
        filter.setSort(ProductSort.values()[rnd.nextInt(ProductSort.values().length)]);
        products = simulator.getService().getProducts(filter);
    }

    protected void doProdutViewDetails() {
        if (getOrFetchProductList()) {
            product = pickRandomProduct();
            simulator.getService().getProductDetails(product.getId());
        }
    }

    protected void doProductAddToCart() {
        if (getOrFetchProduct()) {
            simulator.getService().addToCart(customer.getId(), product.getId(), rnd.nextInt(10) + 1);
            cart = simulator.getService().getCustomerCart(customer.getId());
        }
    }

    protected void doProductAddReview() {
        if (getOrFetchProduct()) {
            int rating = rnd.nextInt(5) + 1;
            String email = "Customer" + customer.getId() + "@test.com";
            String title = "Review " + System.currentTimeMillis();
            String comments = "This review was added by a load simulation tool.";
            simulator.getService().addProductReview(customer.getId(), pickRandomProduct().getId(), title, comments, email, rating);
        }
    }

    protected void doCartView() {
        simulator.getService().getCustomerCart(customer.getId());
    }

    protected void doCartUpdate() {
        if (getOrFetchNonEmptyCart()) {
            Map<Integer, Integer> updates = new HashMap<Integer, Integer>();
            for (CartSelection item : cart.getResult()) {
                updates.put(item.getProduct().getId(), rnd.nextInt(10));
            }
            simulator.getService().updateCart(customer.getId(), updates);
        }
    }

    protected void doCartCheckout() {
        if (!getOrFetchNonEmptyCart()) {
            return;
        }
        simulator.getService().checkout(customer.getId());
        cart = null;
    }
    
    protected void doRunReport() {
        simulator.getService().getStorefrontStats(0);
    }

    protected boolean getOrFetchCategories() {
        if (categories == null) {
            doBrowse();
        }
        return !categories.getResult().isEmpty();
    }

    protected boolean getOrFetchProductList() {
        if (products == null) {
            doBrowse();
        }

        return !products.getResult().isEmpty();
    }

    protected boolean getOrFetchProduct() {
        if (product == null) {
            doProdutViewDetails();
            if (product == null) {
                return false;
            }
        }
        return true;
    }

    protected boolean getOrFetchNonEmptyCart() {
        if (cart == null || cart.getResult().isEmpty()) {
            doProductAddToCart();
        }
        return !cart.getResult().isEmpty();
    }

    protected Category pickRandomCategory() {
        if (categories == null) {
            categories = simulator.getService().getCategories();
        }
        return (categories == null || categories.getResult().isEmpty()) ? null :
                categories.getResult().get(rnd.nextInt(categories.getResult().size()));
    }

    protected Product pickRandomProduct() {
        return (products == null || products.getResult().isEmpty()) ? null :
                products.getResult().get(rnd.nextInt(products.getResult().size()));
    }
}