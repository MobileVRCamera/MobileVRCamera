import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.aulbachscheuerpflug.mobileVrCamera.uiAssets.Black
import com.aulbachscheuerpflug.mobileVrCamera.uiAssets.InfoGrey
import com.aulbachscheuerpflug.mobileVrCamera.uiAssets.White
import com.aulbachscheuerpflug.mobileVrCamera.utils.playSystemSound

@Composable
fun InfoPopUp(
    onCloseButton: (() -> Unit)? = null,
    descriptionText: String,
    titleText: String,
    offsetX: Int = 60,
    offsetY: Int = 550,
    width: Int = 275,
    height: Int = 80,
    fontSize: Int = 25,
    titleFontSize: Int = 35,
    rotateDegrees: Float = 0f,
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .absoluteOffset(x = offsetX.dp, y = offsetY.dp)
            .size(width = width.dp, height = height.dp)
            .zIndex(10f),
        elevation = 0.dp,
        backgroundColor = Color.Transparent
    )
    {
        Row(
            modifier = Modifier
                .background(color = InfoGrey, shape = RoundedCornerShape(size = 27.dp))
                .clip(shape = RoundedCornerShape(27.dp)),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = titleText,
                fontSize = titleFontSize.sp,
                color = White,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(top = 20.dp, start = 20.dp, end = 20.dp)
                    .rotate(rotateDegrees)
                    .absoluteOffset(0.dp, (-1 * height / 2.5).dp)
                    .verticalScroll(rememberScrollState())
                    .align(Alignment.CenterVertically)
            )

        }
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = descriptionText,
                fontSize = fontSize.sp,
                color = White,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .size(width.dp, (0.5 * height).dp)
                    .padding(start = 20.dp, end = 20.dp)
                    .rotate(rotateDegrees)
                    .verticalScroll(rememberScrollState())
                    .align(Alignment.CenterVertically)
            )
        }
        if (onCloseButton != null) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    modifier = Modifier
                        .rotate(rotateDegrees)
                        .absoluteOffset(0.dp, (height / 3).dp)
                        .clip(shape = RoundedCornerShape(27.dp))
                        .border(1.dp, Black, RoundedCornerShape(27.dp))
                        .background(color = White, shape = RoundedCornerShape(27.dp)),
                    colors = ButtonDefaults.buttonColors(backgroundColor = White),
                    onClick = {
                        playSystemSound(context)
                        onCloseButton()
                    }
                )
                {
                    Text(
                        text = "Okay",
                        fontSize = fontSize.sp,
                        textAlign = TextAlign.Justify,
                        color = Black
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun InfoPopUpPreview() {
    InfoPopUp(
        null,
        "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod",
        "Lorem ipsum",
    )
}