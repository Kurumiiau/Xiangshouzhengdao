package com.example.xiangshouzhengdao.ui.profile

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.createBitmap
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.lifecycleScope
import com.example.xiangshouzhengdao.R
import com.example.xiangshouzhengdao.data.local.DataRepository
import com.example.xiangshouzhengdao.databinding.ActivityEditProfileBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch
import java.io.File

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding

    private lateinit var outputImage: File
    private lateinit var imageUri: Uri

    private var pendingGalleryBitmap: Bitmap? = null
    private val galleryWritePermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        val bitmap = pendingGalleryBitmap
        pendingGalleryBitmap = null
        if (granted && bitmap != null) saveImageToGallery(bitmap)
        else if (!granted) Toast.makeText(this, getString(R.string.storage_permission_toast), Toast.LENGTH_SHORT).show()
    }


    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) openCamera() else Toast.makeText(this, getString(R.string.cam_permission_toast), Toast.LENGTH_SHORT).show()
    }

    private val takePhotoLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(imageUri))
                binding.ivAvatar.setImageBitmap(rotateIfRequired(bitmap))
                saveAvatarPath(outputImage.absolutePath)
            } catch (e: Exception) {
                Toast.makeText(this, getString(R.string.photo_fail), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val pickPhotoLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                try {
                    contentResolver.openInputStream(uri)?.use { stream ->
                        val bitmap = BitmapFactory.decodeStream(stream)
                        bitmap?.let {
                            val rotatedBitmap = rotateBitmapIfRequired(uri, it)
                            binding.ivAvatar.setImageBitmap(rotatedBitmap)
                            val savedPath = saveBitmapToLocal(rotatedBitmap)
                            if (savedPath != null) saveAvatarPath(savedPath)
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(this, getString(R.string.img_load_fail), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        restoreCameraState(savedInstanceState)

        initViews()
        loadUserData()
        loadAvatar()
    }

    // 相机位于另一个Activity，后台回收/旋转后返回仍需找到同一个输出文件。
    private fun restoreCameraState(state: Bundle?) {
        val path = state?.getString("pending_camera_path") ?: return
        outputImage = File(path)
        imageUri = FileProvider.getUriForFile(this, "${this.packageName}.fileprovider", outputImage)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (::outputImage.isInitialized) outState.putString("pending_camera_path", outputImage.absolutePath)
        super.onSaveInstanceState(outState)
    }

    private fun initViews() {
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.ivAvatar.setOnClickListener { showBottomDialog() }

        binding.btnSaveNickname.setOnClickListener {
            val newNickname = binding.etNickname.text.toString().trim()
            if (newNickname.isEmpty()) {
                Toast.makeText(this, getString(R.string.nickname_empty), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            lifecycleScope.launch {
                val repository = DataRepository.getInstance(this@EditProfileActivity)
                val user = repository.getCurrentUser()
                if (user != null) {
                    val success = repository.updateNickname(user.userId, newNickname)
                    Toast.makeText(this@EditProfileActivity,
                        if (success) getString(R.string.nickname_saved) else getString(R.string.nickname_save_fail), Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.btnChangePassword.setOnClickListener {
            val oldPwd = binding.etOldPassword.text.toString()
            val newPwd = binding.etNewPassword.text.toString()
            val confirmPwd = binding.etConfirmPassword.text.toString()

            if (oldPwd.isEmpty() || newPwd.isEmpty() || confirmPwd.isEmpty()) {
                Toast.makeText(this, getString(R.string.pwd_fields_empty), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (newPwd != confirmPwd) {
                Toast.makeText(this, getString(R.string.pwd_mismatch), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (newPwd.length < 6) {
                Toast.makeText(this, getString(R.string.pwd_too_short), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val repository = DataRepository.getInstance(this@EditProfileActivity)
                val user = repository.getCurrentUser()
                if (user != null) {
                    val success = repository.changePassword(user.userId, oldPwd, newPwd)
                    Toast.makeText(this@EditProfileActivity,
                        if (success) getString(R.string.pwd_changed) else getString(R.string.pwd_wrong), Toast.LENGTH_SHORT).show()
                    if (success) {
                        binding.etOldPassword.text?.clear()
                        binding.etNewPassword.text?.clear()
                        binding.etConfirmPassword.text?.clear()
                    }
                }
            }
        }
    }

    private fun loadUserData() {
        lifecycleScope.launch {
            val repository = DataRepository.getInstance(this@EditProfileActivity)
            val user = repository.getCurrentUser()
            if (user != null) {
                binding.etNickname.setText(user.nickname)
                binding.tvUsername.text = getString(R.string.ui_username_value, user.username)
            }
        }
    }

    private fun showBottomDialog() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.choosingavatarmenu, FrameLayout(this), false)
        dialog.setContentView(view)

        view.findViewById<android.view.View>(R.id.tv_check_avatar).setOnClickListener {
            dialog.dismiss()
            showBigAvatarDialog()
        }
        view.findViewById<android.view.View>(R.id.tv_take_photo).setOnClickListener {
            dialog.dismiss()
            checkCameraPermissionAndOpen()
        }
        view.findViewById<android.view.View>(R.id.tv_choose_gallery).setOnClickListener {
            dialog.dismiss()
            checkStoragePermissionAndOpen()
        }
        view.findViewById<android.view.View>(R.id.tv_cancel).setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun checkCameraPermissionAndOpen() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> openCamera()
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                AlertDialog.Builder(this).setTitle(getString(R.string.cam_permission_title)).setMessage(getString(R.string.cam_permission_msg))
                    .setPositiveButton(getString(R.string.confirm)) { _, _ -> cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }
                    .setNegativeButton(getString(R.string.cancel), null).show()
            }
            else -> cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun checkStoragePermissionAndOpen() {
        // 系统文档选择器授予所选文件访问权，不需要读取整个相册的权限。
        openGallery()
    }

    private fun openCamera() {
        try {
            val avatarDir = File(filesDir, "avatars")
            if (!avatarDir.exists()) avatarDir.mkdirs()
            outputImage = File(avatarDir, "avatar_${System.currentTimeMillis()}.jpg")
            if (outputImage.exists()) outputImage.delete()
            outputImage.createNewFile()

            imageUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", outputImage)

            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            takePhotoLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.camera_open_fail), Toast.LENGTH_SHORT).show()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
        }
        pickPhotoLauncher.launch(intent)
    }

    private fun showBigAvatarDialog() {
        val bigDialog = Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        val view = layoutInflater.inflate(R.layout.dialog_view_avatar, FrameLayout(this), false)
        val imageView = view.findViewById<android.widget.ImageView>(R.id.iv_big_avatar)
        val saveBtn = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btn_save_to_gallery)

        binding.ivAvatar.drawable?.let { imageView.setImageDrawable(it) }
            ?: run { imageView.setImageResource(R.drawable.bg_circle) }

        saveBtn?.setOnClickListener {
            val drawable = imageView.drawable ?: return@setOnClickListener
            val bitmap = createBitmap(drawable.intrinsicWidth.coerceAtLeast(1), drawable.intrinsicHeight.coerceAtLeast(1), Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            saveImageToGallery(bitmap)
        }
        view.setOnClickListener { bigDialog.dismiss() }
        bigDialog.setContentView(view)
        bigDialog.show()
    }

    private fun saveImageToGallery(bitmap: Bitmap) {
        // Android 8/9写公共相册仍需运行时权限；Android 10起使用分区存储。
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            pendingGalleryBitmap = bitmap
            galleryWritePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            return
        }
        try {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "Avatar_${System.currentTimeMillis()}.jpg")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Xiangshouzhengdao")
                }
            }
            val resolver = this.contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                ?: error("Cannot create gallery entry")
            try {
                resolver.openOutputStream(uri)?.use { out ->
                    check(bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out))
                } ?: error("Cannot open gallery entry")
            } catch (e: Exception) {
                resolver.delete(uri, null, null)
                throw e
            }
            Toast.makeText(this, getString(R.string.avatar_saved), Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.save_fail), Toast.LENGTH_SHORT).show()
        }
    }

    private fun rotateBitmapIfRequired(uri: Uri, bitmap: Bitmap): Bitmap {
        return try {
            contentResolver.openInputStream(uri)?.use { stream ->
                val exif = ExifInterface(stream)
                val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90)
                    ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180)
                    ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270)
                    else -> bitmap
                }
            } ?: bitmap
        } catch (e: Exception) { bitmap }
    }

    private fun rotateIfRequired(bitmap: Bitmap): Bitmap {
        return try {
            val exif = ExifInterface(outputImage.path)
            val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270)
                else -> bitmap
            }
        } catch (e: Exception) { bitmap }
    }

    private fun rotateBitmap(bitmap: Bitmap, degree: Int): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degree.toFloat())
        val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        bitmap.recycle()
        return rotatedBitmap
    }

    private fun saveBitmapToLocal(bitmap: Bitmap): String? {
        return try {
            val avatarDir = File(filesDir, "avatars")
            if (!avatarDir.exists()) avatarDir.mkdirs()
            val file = File(avatarDir, "avatar_gallery_${System.currentTimeMillis()}.jpg")
            file.outputStream().use { out -> bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out) }
            file.absolutePath
        } catch (e: Exception) { null }
    }

    private fun saveAvatarPath(path: String) {
        lifecycleScope.launch {
            val repository = DataRepository.getInstance(this@EditProfileActivity)
            val userId = repository.getCurrentUser()?.userId ?: "guest"
            getSharedPreferences("avatar_prefs_$userId", Context.MODE_PRIVATE).edit {
                putString("avatar_path", path)
            }
        }
    }

    private fun loadAvatar() {
        lifecycleScope.launch {
            val repository = DataRepository.getInstance(this@EditProfileActivity)
            val userId = repository.getCurrentUser()?.userId ?: "guest"
            val path = getSharedPreferences("avatar_prefs_$userId", Context.MODE_PRIVATE)
                .getString("avatar_path", null)
            if (path != null) {
                val file = File(path)
                if (file.exists()) BitmapFactory.decodeFile(path)?.let { binding.ivAvatar.setImageBitmap(it) }
            }
        }
    }
}