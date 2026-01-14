const sidebar = document.getElementById('sidebar');
const toggleBtn = document.getElementById('sidebarToggle');

toggleBtn.addEventListener('click', () => {
    sidebar.classList.toggle('open');
});

  
window.addEventListener('click', (e) => {
    if (!sidebar.contains(e.target) && !toggleBtn.contains(e.target)) {
      sidebar.classList.remove('open');
    }
});

const loginBtn = document.querySelector('.login-btn');
const loginModal = document.getElementById('loginModal');
const closeBtn = document.querySelector('.close-btn');

loginBtn.addEventListener('click', () => {
    loginModal.classList.add('show');
});

closeBtn.addEventListener('click', () => {
    loginModal.classList.remove('show');
});

window.addEventListener('click', (e) => {
    if (e.target === loginModal) loginModal.classList.remove('show');
});

const addPostBtn = document.getElementById('addPostBtn');
const addPostModal = document.getElementById('addPostModal');
const addCloseBtn = addPostModal.querySelector('.close-btn');

addPostBtn.addEventListener('click', () => {
    addPostModal.classList.add('show');
});

addCloseBtn.addEventListener('click', () => {
    addPostModal.classList.remove('show');
});

window.addEventListener('click', (e) => {
    if (e.target === addPostModal) addPostModal.classList.remove('show');
});

$(document).ready(function() {
    const token = localStorage.getItem("token");
    const now = Date.now();
    const lastChecked = parseInt(localStorage.getItem("tokenChecked")) || 0;
    const SHORT_INTERVAL = Math.floor(Math.random() * 5 * 60 * 1000) + (30 * 60 * 1000);
    if (token) {
        if (!lastChecked || (now - lastChecked) > SHORT_INTERVAL) {
            const formData = new FormData();
            formData.append("Authorization", "Bearer " + token);
            $.ajax({
                url: "http://localhost:8080/api/auth/verify-token",
                type: "POST",
                processData: false,
                contentType: false,
                data: formData,
                headers: {
                    "Authorization": "Bearer " + token
                },
                success: function(valid) {
                    if (valid) {
                        $(".login-btn").hide();
                        localStorage.setItem("tokenChecked", Date.now());
                    } else {
                        $(".login-btn").show();
                        localStorage.removeItem("token");
                        localStorage.removeItem("tokenChecked");
                    }
                },
                error: function(xhr) {
                    console.log("验证 token 出错:", xhr);
                    $(".login-btn").show();
                    $(".logined").hide();
                }
            });
        }else{
             $(".login-btn").hide();
             $(".logined").show();
        }
    } else {
        $(".login-btn").show();
        $(".logined").hide();
    }

    $("#authForm").on("submit", function(e) {
        e.preventDefault();
        $("#submitbtn").prop("disabled", true).text("loading...");
        $.ajax({
            url: "http://localhost:8080/api/auth/login",
            type: "POST",
            processData: false, 
            contentType: false,
            data: new FormData(this),
            success: function(res) {
                const token = res.data;
                localStorage.setItem("token", token);
                location.reload();
            },
            error: function(xhr) {
                alert("invalid gmail or password");
                localStorage.removeItem("token");
                $("#submitbtn").prop("disabled", false).text("登录");
            }
        });

    });
    $("#logout").on('click',()=>{
        localStorage.removeItem("token");
        localStorage.removeItem("tokenChecked");
        location.reload();
    });
  
    let page = 1; 
    let loading = false; 
    let hasMore = true; 
    function loadPost(){
        
         if (!hasMore) return; 
         const token = localStorage.getItem("token");
          $.ajax({
            url: 'http://localhost:8080/api/posts/list',
            type: 'POST',
            headers: { "Authorization": "Bearer " + token },
            data: { page: page },
            success: function(posts) {
                if (!posts || posts.length === 0 || posts.length < 10) {
                    hasMore = false;
                }
                renderPosts(posts);
                page++; 
                loading = false;
            },
            error: function() {
                loading = false;
            }
          });
    }

    $(window).scroll(function() {
        if ($(window).scrollTop() + $(window).height() >= $(document).height() - 50) {
            if (!loading) {
                loading = true;
               loadPost();
            }
        }
    });
     loadPost();



    function renderPosts(posts) {
        posts.forEach(post => {
            const article = $('<article>').addClass('post glass');
            const imgDiv = $('<div>').addClass('post-image');
            if (post.images) {
                try {
                    const imageArray = JSON.parse(post.images); 
                    if (imageArray.length > 0) {
                        imgDiv.append($('<img>').attr('src', imageArray[0])); 
                    }
                } catch (e) {
                    console.error('解析 images 失败', e);
                }
            }
            article.append(imgDiv);
            article.append($('<h2>').text(post.title));

            const metaDiv = $('<div>').addClass('meta');
            if (post.createdAt) {
                const date = post.createdAt.substring(0, 10); 
                metaDiv.append($('<span>').text(date));
            }
            if (post.authorName) metaDiv.append($('<span>').text('· ' + post.authorName));
            article.append(metaDiv);

            if (post.content) {
                let content = post.content;
                if (content.length > 40) {
                    content = content.substring(0, 40) + '...';
                }
                article.append($('<p>').text(content));
            }

            const actionsDiv = $('<div>').addClass('actions');
            actionsDiv.append(
                $('<a>')
                    .addClass('btn')
                    .attr('href', 'post.html?id=' + post.id) 
                    .text('Read')
            );
            if (post.isMine) {
                const editBtn = $('<button>')
                    .addClass('btn secondary edit-btn')
                    .attr('data-post-id', post.id)     
                    .text('Edit');                     

         
                editBtn.on('click', function() {
                    const postId = $(this).data('post-id');
                    window.location.href = 'edit.html?id=' + postId;
                });

                actionsDiv.append(editBtn);
                const deleteBtn = $('<button>')
                    .addClass('btn danger delete-btn') // 可以用 danger 样式
                    .attr('data-post-id', post.id)
                    .text('Delete').on('click', function() {
                        const postId = $(this).data('post-id');
                        deletePost(postId);
                    });

                actionsDiv.append(deleteBtn);
            }
            article.append(actionsDiv);
            $('#postContainer').append(article);
        });
    }
   
    const imageInput = $('#imageInput');
    const previewDiv = $('#imagePreview');
    const MAX_IMAGES = 5;
    imageInput.on('change', function () {
        previewDiv.empty(); 
        let files = Array.from(this.files);
        if (files.length > MAX_IMAGES) {
            alert(`You can upload maximum ${MAX_IMAGES} images`);
            files = files.slice(0, MAX_IMAGES);
            const dataTransfer = new DataTransfer();
            files.forEach(file => dataTransfer.items.add(file));
            this.files = dataTransfer.files;
        }
        files.forEach(file => {
            const reader = new FileReader();
            reader.onload = function (e) {
                const img = $('<img>').attr('src', e.target.result);
                previewDiv.append(img);
            };
            reader.readAsDataURL(file);
        });
    });

    function deletePost(postId) {
    if (!confirm("Are you sure to delete this post")) return;

    $.ajax({
        url: `http://localhost:8080/api/posts/delete/${postId}`,
        type: 'DELETE',
        headers: {
            'Authorization': 'Bearer ' + localStorage.getItem('token')
        },
        success: function(res) {
            alert('post deleted');
            location.reload();
        },
        error: function(xhr) {
            let msg = "delete fail";
            try {
                const err = JSON.parse(xhr.responseText);
                if (err.message) msg = err.message;
            } catch(e) {}
            alert(msg);
        }
    });
}
    $('#addPostForm').on('submit',function(e){
        e.preventDefault();
        const token = localStorage.getItem("token");
        if (!token) {
            alert("You must be logged in to add a post!");
            return;
        }
        const formData = new FormData(this);
        formData.append("Authorization", "Bearer " + token);
        $.ajax({
            url: 'http://localhost:8080/api/posts/create',
            type: 'POST',
            data: formData,
            processData: false,
            contentType: false,
            headers: {
                "Authorization": "Bearer " + token
            },
            success: function(response) {
                alert('Post added successfully!');
                location.reload(); 
            },
            error: function(xhr) {
                console.error('Add post error:', xhr);
                alert('Failed to add post.');
            }
        });



    });

   
});